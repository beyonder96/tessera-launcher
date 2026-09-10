#!/usr/bin/env python3
import os
import sys
import re
import json
import urllib.request
import urllib.parse
import urllib.error

def get_token():
    token = os.environ.get("GITHUB_TOKEN")
    if token:
        return token
    cred_path = os.path.expanduser("~/.git-credentials")
    if os.path.exists(cred_path):
        with open(cred_path) as f:
            for line in f:
                line = line.strip()
                if "github.com" in line:
                    parsed = urllib.parse.urlparse(line)
                    if parsed.password:
                        return parsed.password
    return None

def get_current_version():
    gradle_path = os.path.join(os.path.dirname(__file__), "..", "app", "build.gradle.kts")
    if os.path.exists(gradle_path):
        with open(gradle_path) as f:
            for line in f:
                m = re.search(r'versionName\s*=\s*"([^"]+)"', line)
                if m:
                    return m.group(1)
    return None

def get_release_notes(version):
    readme_path = os.path.join(os.path.dirname(__file__), "..", "README.md")
    if os.path.exists(readme_path):
        with open(readme_path) as f:
            content = f.read()
        pattern = rf'### 🆕 Novidades na v{re.escape(version)}(.*?)(?=\n###|\n<details>|\Z)'
        m = re.search(pattern, content, re.DOTALL)
        if m:
            return f"### 🆕 Novidades na v{version}\n" + m.group(1).strip()
    return f"Release v{version}"

def main():
    token = get_token()
    if not token:
        print("Erro: Token do GitHub não encontrado em GITHUB_TOKEN ou ~/.git-credentials", file=sys.stderr)
        sys.exit(1)

    version = sys.argv[1] if len(sys.argv) > 1 else get_current_version()
    if not version:
        print("Erro: Não foi possível determinar a versão.", file=sys.stderr)
        sys.exit(1)

    tag = f"v{version}" if not version.startswith("v") else version
    repo = "beyonder96/tessera-launcher"
    body = get_release_notes(version.lstrip("v"))

    print(f"==> Criando release {tag} para o repositório {repo}...")
    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github.v3+json",
        "User-Agent": "Tessera-Release-Bot"
    }

    url = f"https://api.github.com/repos/{repo}/releases"
    payload = json.dumps({
        "tag_name": tag,
        "name": tag,
        "body": body,
        "draft": False,
        "prerelease": False
    }).encode("utf-8")

    req = urllib.request.Request(url, data=payload, headers={**headers, "Content-Type": "application/json"})
    upload_url = None
    release_url = None

    try:
        with urllib.request.urlopen(req) as resp:
            rel = json.loads(resp.read().decode())
            release_url = rel.get("html_url")
            upload_url = rel.get("upload_url").split("{")[0]
            print(f"Release criada com sucesso: {release_url}")
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode()
        if e.code == 422 and "already_exists" in err_msg:
            print(f"Release para {tag} já existe, obtendo detalhes...")
            req_get = urllib.request.Request(f"{url}/tags/{tag}", headers=headers)
            with urllib.request.urlopen(req_get) as resp:
                rel = json.loads(resp.read().decode())
                release_url = rel.get("html_url")
                upload_url = rel.get("upload_url").split("{")[0]
                print(f"Release encontrada: {release_url}")
        else:
            print(f"Erro HTTP {e.code} ao criar release: {err_msg}", file=sys.stderr)
            sys.exit(1)

    # Obter assets já enviados na release para evitar conflito de duplicação
    existing_assets = {}
    try:
        assets_url = f"https://api.github.com/repos/{repo}/releases/tags/{tag}"
        with urllib.request.urlopen(urllib.request.Request(assets_url, headers=headers)) as resp:
            rel_info = json.loads(resp.read().decode())
            for a in rel_info.get("assets", []):
                existing_assets[a.get("name")] = a.get("id")
    except Exception:
        pass

    project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    apks = [
        ("tessera-launcher-release.apk", os.path.join(project_root, "apk", "tessera-launcher-release.apk")),
        ("tessera-launcher-debug.apk", os.path.join(project_root, "apk", "tessera-launcher-debug.apk"))
    ]

    for filename, filepath in apks:
        if not os.path.exists(filepath):
            print(f"Aviso: {filepath} não existe, pulando upload.")
            continue

        if filename in existing_assets:
            asset_id = existing_assets[filename]
            print(f"Asset {filename} já existe (id {asset_id}), removendo para re-upload...")
            del_req = urllib.request.Request(
                f"https://api.github.com/repos/{repo}/releases/assets/{asset_id}",
                headers=headers,
                method="DELETE"
            )
            try:
                with urllib.request.urlopen(del_req) as del_resp:
                    pass
            except Exception as e:
                print(f"Aviso ao deletar asset anterior: {e}")

        size_mb = os.path.getsize(filepath) / (1024 * 1024)
        print(f"Enviando {filename} ({size_mb:.2f} MB)...")
        with open(filepath, "rb") as f:
            data = f.read()

        upload_target = f"{upload_url}?name={filename}"
        upload_req = urllib.request.Request(
            upload_target,
            data=data,
            headers={
                **headers,
                "Content-Type": "application/vnd.android.package-archive"
            }
        )

        try:
            with urllib.request.urlopen(upload_req) as resp:
                asset_info = json.loads(resp.read().decode())
                print(f"Asset {filename} enviado: {asset_info.get('browser_download_url')}")
        except urllib.error.HTTPError as e:
            print(f"Erro ao enviar {filename}: {e.code} - {e.read().decode()}", file=sys.stderr)

    print(f"\n✅ Release {tag} finalizada no GitHub: {release_url}")

if __name__ == "__main__":
    main()
