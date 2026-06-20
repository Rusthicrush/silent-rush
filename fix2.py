path = "gradle/libs.versions.toml"
with open(path, "r") as f:
    content = f.read()

old = 'googleDevtoolsKsp = "2.3.5"'
new = 'googleDevtoolsKsp = "2.2.10-2.0.2"'

if old in content:
    content = content.replace(old, new)
    with open(path, "w") as f:
        f.write(content)
    print("OK: KSP version fixed")
else:
    print("SKIP: pattern not found")
