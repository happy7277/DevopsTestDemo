import os
from collections.abc import Iterator
from contextlib import asynccontextmanager

from fastapi import Depends, FastAPI, HTTPException, Query
from pydantic import BaseModel, ConfigDict, Field
from sqlalchemy import Integer, String, create_engine, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column


class Base(DeclarativeBase):
    pass


class Product(Base):
    __tablename__ = "products"
    id: Mapped[int] = mapped_column(primary_key=True)
    sku: Mapped[str] = mapped_column(String(32), unique=True)
    name: Mapped[str] = mapped_column(String(100))
    price_cents: Mapped[int] = mapped_column(Integer)


class ProductInput(BaseModel):
    sku: str = Field(pattern=r"^[A-Z0-9-]{1,32}$")
    name: str = Field(min_length=1, max_length=100)
    price_cents: int = Field(ge=0, le=100_000_000)


class ProductView(ProductInput):
    model_config = ConfigDict(from_attributes=True)
    id: int


def create_app(database_url: str | None = None) -> FastAPI:
    url = database_url or os.getenv("PRODUCT_DB_URL", "sqlite:///./products.db")
    engine = create_engine(url)

    @asynccontextmanager
    async def lifespan(_: FastAPI):
        Base.metadata.create_all(engine)
        try:
            yield
        finally:
            engine.dispose()

    api = FastAPI(title="Demo Products", lifespan=lifespan)

    def session() -> Iterator[Session]:
        yield Session(engine)

    @api.get("/health")
    def health():
        return {"status": "UP"}

    @api.get("/products", response_model=list[ProductView])
    def products(
        q: str = Query("", max_length=100),
        offset: int = Query(0, ge=0, le=1_000_000),
        limit: int = Query(20, ge=1, le=100),
        db: Session = Depends(session),
    ):
        query = select(Product).order_by(Product.id)
        if q:
            query = query.where(Product.name.icontains(q, autoescape=True))
        return db.scalars(query.offset(offset).limit(limit)).all()

    @api.get("/products/{product_id}", response_model=ProductView)
    def product(product_id: int, db: Session = Depends(session)):
        item = db.get(Product, product_id)
        if item is None:
            raise HTTPException(404, "Product not found")
        return item

    @api.post("/products", response_model=ProductView, status_code=201)
    def create_product(data: ProductInput, db: Session = Depends(session)):
        item = Product(**data.model_dump())
        db.add(item)
        try:
            db.commit()
        except IntegrityError as error:
            db.rollback()
            raise HTTPException(409, "SKU already exists") from error
        db.refresh(item)
        return item

    return api


app = create_app()
