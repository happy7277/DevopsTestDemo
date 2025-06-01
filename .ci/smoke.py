import io
import json
import time
import uuid
import zipfile
from datetime import datetime, timezone
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

JAVA = "http://demo-app:8080/api"
PYTHON = "http://demo-python:8000"
FRONTEND = "http://demo-frontend"


def call(url, expected=200, method="GET", data=None, token=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = Request(url, json.dumps(data).encode() if data is not None else None, headers, method=method)
    try:
        response = urlopen(req, timeout=5)
    except HTTPError as error:
        response = error
    with response:
        body = response.read()
        assert response.status == expected, (url, expected, response.status, body[:200])
        if "application/json" in response.headers.get("Content-Type", ""):
            return json.loads(body)
        return body


def wait_for(url):
    for _ in range(60):
        try:
            call(url)
            return
        except (URLError, AssertionError, TimeoutError):
            time.sleep(2)
    raise SystemExit(f"Service unavailable: {url}")


def users():
    call(JAVA + "/users", 401)
    login = call(JAVA + "/auth/login", method="POST", data={
        "email": "admin@example.com", "password": "example-password",
    })
    token = login["token"]
    tag = uuid.uuid4().hex[:8]
    sample = {"name": "Demo " + tag, "email": tag + "@example.com", "status": "ACTIVE"}
    item = call(JAVA + "/users", 201, "POST", sample, token)
    resource = f"{JAVA}/users/{item['id']}"
    assert call(resource, token=token)["email"] == sample["email"]
    assert call(JAVA + "/users?q=" + tag, token=token)["total"] == 1
    assert call(resource, method="PUT", data={**sample, "status": "DISABLED"}, token=token)["status"] == "DISABLED"
    today = datetime.now(timezone.utc).date()
    dashboard = call(f"{JAVA}/dashboard?from={today}&to={today}", token=token)
    assert len(dashboard) == 1 and dashboard[0]["disabled"] >= 1
    workbook = call(JAVA + "/users/export?q=" + tag, token=token)
    with zipfile.ZipFile(io.BytesIO(workbook)) as archive:
        assert "xl/worksheets/sheet1.xml" in archive.namelist()
    call(resource, 204, "DELETE", token=token)
    call(resource, 404, token=token)
    actions = {entry["action"] for entry in call(JAVA + "/audit", token=token)}
    assert {"LOGIN", "USER_CREATE", "USER_UPDATE", "USER_EXPORT", "USER_DELETE"} <= actions


def products():
    sku = "DEMO-" + uuid.uuid4().hex[:8].upper()
    item = call(PYTHON + "/products", 201, "POST", {"sku": sku, "name": sku, "price_cents": 250})
    assert call(f"{PYTHON}/products/{item['id']}") == item
    assert call(f"{PYTHON}/products?q={sku.lower()}") == [item]


if __name__ == "__main__":
    for url in [JAVA + "/health", PYTHON + "/health", FRONTEND + "/api/health"]:
        wait_for(url)
    assert b'id="root"' in call(FRONTEND + "/")
    users()
    products()
    print("Compose smoke checks passed")
