from fastapi.testclient import TestClient
from app.main import create_app


def test_create_get_search_and_conflict(tmp_path):
    url = f"sqlite:///{(tmp_path / 'products.db').as_posix()}"
    payload = {"sku": "DEMO-1", "name": "Demo 10% Notebook", "price_cents": 250}
    with TestClient(create_app(url)) as client:
        response = client.post("/products", json=payload)
        assert response.status_code == 201
        item = response.json()
        assert client.get(f"/products/{item['id']}").json() == item
        assert client.get("/products", params={"q": "10% notebook"}).json() == [item]
        assert client.get("/products", params={"q": "_"}).json() == []
        assert client.post("/products", json=payload).status_code == 409
        assert client.get("/products/999").status_code == 404


def test_invalid_price_and_pagination(tmp_path):
    url = f"sqlite:///{(tmp_path / 'products.db').as_posix()}"
    with TestClient(create_app(url)) as client:
        assert client.get("/products?limit=0").status_code == 422
        assert client.post("/products", json={
            "sku": "DEMO-2", "name": "Demo Pen", "price_cents": -1,
        }).status_code == 422
