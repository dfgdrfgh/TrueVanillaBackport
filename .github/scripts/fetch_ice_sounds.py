"""Fetch SHA-1-pinned official 26.4 sound samples for the integrated Fabric module."""
import concurrent.futures
import hashlib
import json
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
manifest = json.loads((ROOT / '.github/ice-caves-sounds.json').read_text())
assets = ROOT / 'vendor/ice-caves/src/main/resources/assets/ice_caves'
sounds_file = assets / 'sounds.json'
sounds = json.loads(sounds_file.read_text())
sounds.update(manifest['events'])
sounds_file.write_text(json.dumps(sounds, indent=2) + '\n')

def fetch(item):
    name, entry = item
    path = assets / 'sounds' / name
    expected = entry['hash']
    if path.exists() and hashlib.sha1(path.read_bytes()).hexdigest() == expected:
        return
    with urllib.request.urlopen(f'https://resources.download.minecraft.net/{expected[:2]}/{expected}', timeout=90) as response:
        data = response.read()
    if hashlib.sha1(data).hexdigest() != expected or len(data) != entry['size']:
        raise RuntimeError(f'Vanilla sound hash/size mismatch: {name}')
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)

with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
    list(pool.map(fetch, manifest['files'].items()))
print(f"Verified {len(manifest['files'])} official samples for {len(manifest['events'])} ice sound events")
