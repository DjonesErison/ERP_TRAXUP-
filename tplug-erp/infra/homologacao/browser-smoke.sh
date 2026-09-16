#!/usr/bin/env bash
set -Eeuo pipefail
url="${1:?URL obrigatoria}"
browser=$(command -v google-chrome || command -v chromium || command -v chromium-browser)
scratch=$(mktemp -d)
trap 'rm -rf "$scratch"' EXIT
timeout 45s "$browser" --headless --no-sandbox --disable-dev-shm-usage \
  --user-data-dir="$scratch/profile" --virtual-time-budget=10000 \
  --dump-dom "$url" > "$scratch/page.html" 2> "$scratch/browser.log"
python3 - "$scratch/page.html" <<'PY'
import sys
from html.parser import HTMLParser
from pathlib import Path

class LoginParser(HTMLParser):
    def __init__(self):
        super().__init__()
        self.fields = {}
        self.form = False
        self.texts = []
    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if tag == 'form' and 'login-card' in attrs.get('class', '').split():
            self.form = True
        if tag == 'input':
            self.fields[attrs.get('name')] = attrs.get('type', 'text')
    def handle_data(self, data):
        self.texts.append(data)

parser = LoginParser()
parser.feed(Path(sys.argv[1]).read_text())
assert parser.form, 'O navegador nao renderizou o formulario de login.'
assert parser.fields.get('tenantId') == 'text', 'Campo de tenant ausente.'
assert parser.fields.get('email') == 'email', 'Campo de email ausente.'
assert parser.fields.get('senha') == 'password', 'Campo de senha ausente.'
assert 'Acesse seu ERP' in ''.join(parser.texts), 'Titulo de login ausente.'
print('Navegador: formulario de login renderizado com tenant, email e senha.')
PY
