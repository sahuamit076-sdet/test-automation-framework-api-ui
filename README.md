# TaPS Test Automation Framework

A modular **Test Automation Framework** built with **Java 17**, **Maven**, and **TestNG**. Supports multi-module structure, database testing, reporting, and parallel execution.

---

## 📑 Table of Contents
1. [Prerequisites](#prerequisites)
2. [Getting Started](#🚀-getting-started)
3. [Project Structure](#📂-project-structure)
4. [Key Features](#⚡-key-features)
5. [Common Module Overview](#📂-common-module)
6. [Reports](#📊-reports)
7. [Git Pre-commit Hook](#🛡️-git-pre-commit-hook-block-large-files)
8. [Contributing](#🤝-contributing)
9. [Troubleshooting / FAQ](#❓-troubleshooting--faq)
10. [License](#license)
11. [Contact](#📬-contact)

---

## 🛠️ Prerequisites
- ☕ **Java 17 (JDK 17+)**
- 🔨 **Apache Maven 3.8+**
- 🌱 **Git**

---

## 🚀 Getting Started
1. **Clone the repo:**
   ```sh
   git clone https://user@bitbucket.org/zetaengg/itp-automation.git
   ```
2. **Run the setup script (required for pre-commit hook):**
   ```sh
   sh setup-hook.sh
   ```
3. **Build the project:**
   ```sh
   mvn clean install -DskipTests
   ```
4. **Run tests:**
   - All tests: `mvn test`
   - Specific module (e.g. upi): `mvn -pl upi test`

---

## 📂 Project Structure

```bash
itp-automation/
├── pom.xml               # Parent POM (dependency management, plugin config)
├── common/               # Core reusable library (shared across modules)
├── acs/                  # ACS test automation
├── card-channels/              # VISA, MasterCard, RuPay transaction tests
├── cms/                  # Card Management System tests
├── extracts-reports/     # Extracts validation & reporting
├── frm/                  # Fraud Risk Management tests
├── pluxee/               # Pluxee studio tests
├── upi/                  # API tests (REST, GraphQL for UPI services)
├── ui/                   # UI automation (Selenium / Playwright)
├── clusters/        # CLUSTERS integrations (AURA, RUBY, AERIES, ACROPOLIS etc.)
└── qa-reporting-hub/     # Custom reporting, TestNG listeners, dashboards, email reports
```

---

## ⚡ Key Features

- ✅ **Multi-Module Architecture** – Clean separation of feature (cards, upi, cms ... etc).
- 🛠️ **Reusable Utilities** – Shared helpers in `common/`.
- 🗄️ **Database Support** – PostgreSQL, Redshift with **HikariCP** pooling.
- 🧠 **Lightweight Singleton & Resource Context** – Provides a singleton/resource manager inspired by `Spring Boot's ApplicationContext`, enabling efficient caching of objects without dependency injection.
- 🌐 **API Testing** – fluent builder-based immutable ApiRequest execution 
- 🕵️ **Auto-Managed Silent SoftAssert** for API tests with Fluent API and ThreadLocal reuse
- 🖥️ **UI Testing** – Selenium/Playwright-based tests in `ui/`.
- 📊 **Reporting** – Custom reporting hub with TestNG listeners, Allure Report.
- 🤖 **Jenkins Integration** – Pipelines use `helper.groovy` for shared logic and utility functions.
- 🏷️ **Dynamic Property Resolution** – Property files resolve values at runtime, supporting references like `${tenant.token}` in other properties.
- 📁 **Cross-Module Resource Scanning & Caching** – Automatically scans and caches property files and other resources under `resources` directories across all modules for unified, efficient configuration and data access.
- 🧹 **Checkstyle Validation at Pre-commit** – Ensures code style compliance before every commit using a shared git pre-commit hook.
---

## 📂 Common Module

```bash
common/
└── in.zeta.qa/
    ├── constants/         # Global constants (endpoints, error codes, currencies, etc.)
    │   ├── annotation/    # Custom annotations
    │   ├── endpoints/     # API endpoint definitions
    │   │     ├── ApiEndpoint(I).java
    │   │     └── ServiceEndpint.java
    │   ├── CommonConstants.java
    │   ├── FilePaths.java
    │   └── ErrorConstants.java
    │
    ├── entity/            # POJOs / DTOs for test data & serialization
    │   ├── Request models
    │   ├── Response models
    │   └── Domain-specific entities
    │
    ├── services/ # Wrappers around reusable service calls
    │   
    └── utils/             # Shared utility functions
        ├── concurrency/   # Thread mgmt, locks, async helpers
        ├── customdeserializer/ # JSON/XML custom deserializers
        ├── db/            # DB utilities (connection pool, queries, schema mgmt)
        ├── dbt/           # DBT (data build tool) helpers
        ├── exceptions/    # Custom exception classes
        ├── fileUtils/     # File readers/writers (CSV, JSON, Properties, Excel)
        ├── hmac/          # HMAC signing, encryption helpers
        ├── misc/          # Miscellaneous reusable helpers
        ├── rest/          # REST clients, request builders, response handlers
        ├── security/      # Security utilities (hashing, JWT, auth tokens)
        ├── sftpManager/   # SFTP connections, file upload/download
        ├── testListeners/ # TestNG listeners, hooks, retry logic
        └── validators/    # Assertion helpers & validators
```

---

## 📊 Reports
After execution, reports are available at:
- 📝 TestNG HTML Report → `target/surefire-reports/index.html`
- 🎨 Allure plugin link in Jenkins → `jenkins_build/<build_id>/allure`
- 📂 Execution Logs → `logs/`

---

## 🛡️ Git Pre-commit Hook: Block Large Files
To prevent accidental commits of files larger than 5MB, a shared pre-commit hook is provided in `.githooks/pre-commit`.

**How to activate:**
1. Run the setup script from the project root:
   ```sh
   sh setup-hook.sh
   ```
   This will configure your local git to use the shared hooks directory and make the hook executable.
2. Now, any attempt to commit a file larger than 5MB will be blocked.

> This ensures consistency and prevents large files from being added to the repository.

---

## 🤝 Contributing
- Fork the repo and create your branch from `main`.
- Jenkins pipelines use `helper.groovy` for shared logic and utility functions in Jenkinsfiles.
- Property files support runtime value resolution, allowing dynamic references (e.g., `${tenant.token}`) in other properties.
- Avoid committing sensitive values (such as tokens or secrets) to source control; resolve them securely at runtime.
---

## ❓ Troubleshooting / FAQ
- **Java version errors:** Ensure you are using JDK 17+.
- **Maven build fails:** Check for missing dependencies or incompatible plugin versions.
- **Lombok Failed:** Check for annotation-processor is enabled & lombok plugin is installed.
- **Pre-commit hook not working:** Make sure you ran `sh setup-hook.sh` and the hook is executable.
- **Large file commit blocked:** Reduce file size below 5MB or use external storage.
- **Compiler heap memory errors:** If you see errors related to Java compiler memory (e.g., "Java heap space" during compilation), increase the compiler heap size in IntelliJ:
  - Go to `File > Settings (Preferences) > Build, Execution, Deployment > Compiler > Java Compiler`.
  - Set the **"Build process heap size (MB)"** to a higher value (e.g., 1024 or 2048).
  - Click **Apply** and **OK**.
  - Rebuild the project.

---

## 📬 Contact
For questions or support, contact the maintainers at [amitsahu@zeta.tech].
