# Agentic DevOps 竞品评测 Demo

Java 17 + Spring Boot 3.1.12 + MyBatis-Plus 3.5.7，React 18 + TypeScript + Vite + Ant Design 5 + ECharts，Python 3.11 + FastAPI + SQLAlchemy。Java 使用 MySQL 8 和 Redis，商品服务默认使用 SQLite。

## 启动全栈

安装 Docker 和 Compose v2，在根目录执行：

```bash
docker compose up --build
```

- 用户运营台 http://localhost:5173 ，账号 `admin@example.com`，密码 `example-password`。
- Java 健康检查 http://localhost:8080/api/health 。
- 商品 API 文档 http://localhost:8000/docs 。商品库初始为空。
- `docker compose down` 停止，数据卷保留。`docker compose down -v` 会清空演示数据。

演示账号与密钥只用于这套本地数据。用户 CRUD 操作通过 API 执行，前端提供登录、检索分页、导出和看板。

## 验证基线

使用 Git、Bash、JDK 17、Maven 3.9、Node.js 22 和 Python 3.11。Windows 可从 Git Bash 执行。Python 测试可以使用项目虚拟环境。

```bash
mvn clean compile
mvn test
npm --prefix demo-frontend ci
npm --prefix demo-frontend run build
npm --prefix demo-frontend test
python3 -m venv demo-python/.venv
# Linux/macOS；Windows Git Bash 改用 source demo-python/.venv/Scripts/activate
source demo-python/.venv/bin/activate
pip install -r demo-python/requirements.txt
(cd demo-python && python -m pytest -q)
python .ci/check-sizes.py
(cd demo-frontend && npx playwright install chromium && npm run test:ui)
```

浏览器测试使用受控 API 响应。全栈使用 `bash .ci/run.sh integration-test` 启动服务并验证真实 CRUD、XLSX、审计、看板及商品 API。运行全栈检查前请释放 5173、8080 和 8000。

## Java API

| 方法和路径 | 用途 |
| --- | --- |
| GET /api/health | 进程健康 |
| POST /api/auth/login | 邮箱、密码换取 JWT |
| GET /api/users?page=1&size=10&q=ada | 用户分页，size 1–100 |
| GET /api/users/{id} | 用户详情 |
| POST /api/users | 创建，201 |
| PUT /api/users/{id} | 更新 |
| DELETE /api/users/{id} | 删除，204 |
| GET /api/users/export?q=ada | XLSX 导出 |
| GET /api/dashboard?from=2025-01-01&to=2025-01-07 | UTC 按日数据 |
| GET /api/audit?limit=20 | 最近审计 |

健康检查和登录为公开入口。其他路径要求 `Authorization: Bearer <token>`。用户写入 JSON 示例：

```json
{"name":"Demo Ada","email":"ada-new@example.com","status":"ACTIVE"}
```

商品服务提供 `POST /products`、`GET /products/{product_id}` 和 `GET /products?q=notebook&offset=0&limit=20`。创建示例：

```json
{"sku":"DEMO-1","name":"Demo Notebook","price_cents":250}
```

## CI 与评测

GitHub Actions 自动读取 `.github/workflows/ci.yml`。GitLab 从根 `.gitlab-ci.yml` 加载 `.ci/pipeline.yml`，集成和发布阶段需要允许 Docker-in-Docker 的 Linux x86_64 runner。两个 CI 调用相同的阶段脚本。Sonar 配置需要 `SONAR_HOST_URL`、`SONAR_PROJECT_KEY` 和保密变量 `SONAR_TOKEN`，三项均省略时该阶段输出跳过原因。`deploy` 在 main 上执行临时 Compose 发布与冒烟验证，完成后清理环境。`package` 归档 JAR、前端静态站点和 Python 服务源码。

在下载包中运行 `bash setup-demo.sh <REMOTE_URL>`。脚本先验证基线，再在 `demo-repository/` 创建独立种子仓并原子推送 85 个分支。请使用空的远程仓库及有建分支权限的账号。Git Bash 可通过 `DEMO_PYTHON_COMMAND=python` 选择解释器，通过 `DEMO_REPO_DIR` 选择种子目录。同一基线可安全重跑脚本。更新基线后使用新的空种子目录。

完整目录树见下载包中的 `DIRECTORY.md`。`docs/` 收录 10 份需求。评测方从下载包中的 `issues/` 和 `evaluation/` 读取缺陷报告、参考答案和端到端任务。这两个目录只随评测包交付。MR 与 Issue 可根据评测产品和任务范围在目标平台创建。

85 个分支含 main、20 个 feature/bugfix/review/ci-fix 分支、30 个代码缺陷、25 个流水线故障，以及 9 个 `scenario/*` 分支。Go 只用于独立评审题，在对应分支运行 `GO111MODULE=off go test ./review-go`。
