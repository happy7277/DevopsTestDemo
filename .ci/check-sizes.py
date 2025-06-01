from pathlib import Path

limits = [
    (Path("demo-app/src"), {".java"}, 120),
    (Path("demo-frontend"), {".ts", ".tsx", ".mjs", ".css", ".js"}, 150),
]
for root, extensions, maximum in limits:
    files = list(root.glob("*"))
    if root.name == "demo-frontend":
        for folder in ("src", "tests", "e2e"):
            files.extend((root / folder).rglob("*"))
    else:
        files.extend(root.rglob("*"))
    for file in files:
        if file.suffix in extensions and len(file.read_text(encoding="utf-8").splitlines()) > maximum:
            raise SystemExit(f"{file} exceeds {maximum} lines")
