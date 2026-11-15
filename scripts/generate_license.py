#!/usr/bin/env python3
"""
Gerador em Lote de Chaves Criptográficas Offline (HMAC-SHA256) — Tessera Launcher

Uso:
  python3 scripts/generate_license.py --type GIFT --days 5 --count 50
  python3 scripts/generate_license.py --type MENS --count 200 --format csv --out mensal.csv
  python3 scripts/generate_license.py --type SEMS --count 100
  python3 scripts/generate_license.py --type ANUL --count 100
  python3 scripts/generate_license.py --type LIFE --count 100
  python3 scripts/generate_license.py --type MSTR --count 1
  python3 scripts/generate_license.py --verify TESS-GIFT-5ABC-XYZ1
"""

import argparse
import csv
import hashlib
import hmac
import secrets
import sys
from typing import Optional, Tuple

BASE32_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
KEY_PREFIX = "TESS"
SECRET_KEY = b"Tessera::2026::Edt0r1al#Launch!Vault$K3y"

KEY_TYPES = {
    "GIFT": ("Cortesia (3 a 7 dias)", None),
    "MENS": ("Pro Mensal", 30),
    "SEMS": ("Pro Semestral", 180),
    "ANUL": ("Pro Anual", 365),
    "LIFE": ("Pro Vitalicio", None),
    "MSTR": ("Master Desenvolvedor", None),
}

GIFT_MIN_DAYS = 3
GIFT_MAX_DAYS = 7


def compute_signature(key_type: str, data4: str) -> str:
    msg = f"{key_type}-{data4}".encode("utf-8")
    digest = hmac.new(SECRET_KEY, msg, hashlib.sha256).digest()
    top24 = int.from_bytes(digest[:3], "big")
    c0 = BASE32_ALPHABET[(top24 >> 19) & 0x1F]
    c1 = BASE32_ALPHABET[(top24 >> 14) & 0x1F]
    c2 = BASE32_ALPHABET[(top24 >> 9) & 0x1F]
    c3 = BASE32_ALPHABET[(top24 >> 4) & 0x1F]
    return f"{c0}{c1}{c2}{c3}"


def generate_key(key_type: str, gift_days: Optional[int] = None) -> Tuple[str, Optional[int]]:
    key_type = key_type.upper()
    if key_type not in KEY_TYPES:
        raise ValueError(f"Tipo inválido '{key_type}'. Opções: {', '.join(KEY_TYPES.keys())}")

    if key_type == "GIFT":
        days = 7 if gift_days is None else gift_days
        if days < GIFT_MIN_DAYS or days > GIFT_MAX_DAYS:
            raise ValueError(
                f"Chaves GIFT devem ter estritamente entre {GIFT_MIN_DAYS} e {GIFT_MAX_DAYS} dias (recebido: {days})."
            )
        chars = [str(days)] + [secrets.choice(BASE32_ALPHABET) for _ in range(3)]
        data4 = "".join(chars)
        duration = days
    else:
        if gift_days is not None:
            raise ValueError(f"--days só pode ser usado com --type GIFT (tipo atual: {key_type}).")
        data4 = "".join(secrets.choice(BASE32_ALPHABET) for _ in range(4))
        duration = KEY_TYPES[key_type][1]

    sig4 = compute_signature(key_type, data4)
    return f"{KEY_PREFIX}-{key_type}-{data4}-{sig4}", duration


def verify_key(raw_key: str) -> Tuple[bool, str]:
    cleaned = "".join(c for c in raw_key.strip().upper() if c.isalnum())
    if len(cleaned) == 16 and cleaned.startswith(KEY_PREFIX):
        normalized = f"{cleaned[0:4]}-{cleaned[4:8]}-{cleaned[8:12]}-{cleaned[12:16]}"
    else:
        normalized = raw_key.strip().upper()

    parts = normalized.split("-")
    if len(parts) != 4 or parts[0] != KEY_PREFIX:
        return False, "Formato inválido (esperado TESS-XXXX-XXXX-XXXX)."

    key_type, data4, sig4 = parts[1], parts[2], parts[3]
    if key_type not in KEY_TYPES:
        return False, f"Tipo desconhecido: {key_type}"
    if len(data4) != 4 or len(sig4) != 4:
        return False, "Blocos DATA/SIG devem ter 4 caracteres."
    if any(c not in BASE32_ALPHABET for c in data4) or any(c not in BASE32_ALPHABET for c in sig4):
        return False, "Caracteres fora do alfabeto Base32 seguro."

    expected_sig = compute_signature(key_type, data4)
    if not hmac.compare_digest(expected_sig, sig4):
        return False, "Assinatura HMAC inválida."

    if key_type == "GIFT":
        if data4[0] not in ("3", "4", "5", "6", "7"):
            return False, f"Duração GIFT fora da faixa permitida (3 a 7 dias): {data4[0]}"
        return True, f"VÁLIDA | {normalized} | GIFT ({data4[0]} dias)"

    label, duration = KEY_TYPES[key_type]
    dur_str = f"{duration} dias" if duration is not None else "Vitalício"
    return True, f"VÁLIDA | {normalized} | {label} ({dur_str})"


def main() -> int:
    parser = argparse.ArgumentParser(description="Gerador de chaves Tessera Launcher")
    parser.add_argument("--type", choices=list(KEY_TYPES.keys()), default="MENS", help="Tipo de licença")
    parser.add_argument("--days", type=int, default=None, help="Dias de cortesia (3 a 7, apenas para GIFT)")
    parser.add_argument("--count", type=int, default=1, help="Quantidade de chaves a gerar")
    parser.add_argument("--format", choices=["text", "csv"], default="text", help="Formato de saída")
    parser.add_argument("--out", type=str, default=None, help="Arquivo de destino opcional")
    parser.add_argument("--verify", type=str, default=None, help="Verificar autenticidade de uma chave")
    args = parser.parse_args()

    if args.verify:
        ok, msg = verify_key(args.verify)
        print(msg)
        return 0 if ok else 1

    if args.count < 1:
        print("Erro: --count deve ser >= 1.", file=sys.stderr)
        return 2

    if args.type == "GIFT":
        check_days = 7 if args.days is None else args.days
        if check_days < GIFT_MIN_DAYS or check_days > GIFT_MAX_DAYS:
            print(
                f"Erro: --days para chaves GIFT deve ser entre {GIFT_MIN_DAYS} e {GIFT_MAX_DAYS} dias.",
                file=sys.stderr,
            )
            return 2
    elif args.days is not None:
        print(f"Erro: --days é exclusivo para --type GIFT (recebido --type {args.type}).", file=sys.stderr)
        return 2

    generated = set()
    rows = []
    attempts = 0
    max_attempts = args.count * 20
    while len(rows) < args.count and attempts < max_attempts:
        attempts += 1
        key, duration = generate_key(args.type, args.days)
        if key not in generated:
            generated.add(key)
            rows.append((key, args.type, duration if duration is not None else "LIFETIME"))

    if args.out:
        with open(args.out, "w", newline="", encoding="utf-8") as f:
            if args.format == "csv":
                writer = csv.writer(f)
                writer.writerow(["key", "type", "duration_days"])
                writer.writerows(rows)
            else:
                for key, _, _ in rows:
                    f.write(f"{key}\n")
        print(f"{len(rows)} chave(s) salva(s) em {args.out}.")
    else:
        if args.format == "csv":
            writer = csv.writer(sys.stdout)
            writer.writerow(["key", "type", "duration_days"])
            writer.writerows(rows)
        else:
            for key, _, _ in rows:
                print(key)

    return 0


if __name__ == "__main__":
    sys.exit(main())
