# Concessionária de Veículos - Monorepo

Sistema completo (Monorepo) de vitrine de veículos e agendamento de test-drives com controle de acesso (RBAC), desenvolvido com **Spring Boot 3 (Java 17+)** no Back-End e **React + TypeScript + TailwindCSS** no Front-End.

---

## 🚀 Passo a Passo para Executar

### Pré-requisitos
- **Docker** e **Docker Compose**
- **Java 17+** e **Maven**
- **Node.js 18+** e **npm**

---

### Passo 1: Subir o Banco de Dados (PostgreSQL)

Na raiz do projeto, execute:

```bash
docker compose up -d postgres
```

> O banco de dados estará pronto na porta `5432`.

---

### Passo 2: Iniciar o Back-End (Spring Boot)

Abra um terminal, acesse a pasta `backend` e execute:

```bash
cd backend
mvn spring-boot:run
```

- A API REST estará rodando em: **`http://localhost:8080`**
- Na primeira execução, o banco é criado e populado automaticamente com marcas, categorias, filiais, veículos e usuários padrão.

---

### Passo 3: Iniciar o Front-End (React + Vite)

Abra outro terminal, acesse a pasta `frontend` e execute:

```bash
cd frontend
npm install
npm run dev
```

- A interface web estará acessível em: **[http://localhost:5173](http://localhost:5173)**

---

## 🔑 Credenciais de Login Padrão

O sistema já vem com contas pré-cadastradas para testes:

| Perfil | Email | Senha | O que pode fazer |
|---|---|---|---|
| **Administrador** | `admin@concessionaria.com` | `admin123` | Acesso total: gestão de operadores, filiais, logs de auditoria e veículos |
| **Operador SP** | `operador1@concessionaria.com` | `operador123` | Aprovar/concluir test-drives e gerenciar catálogo de veículos |
| **Operador PR** | `operador2@concessionaria.com` | `operador123` | Operações da filial Curitiba |

> **Dica:** Na tela de login ([http://localhost:5173/login](http://localhost:5173/login)), utilize os botões **"Admin"** e **"Operador SP"** para preenchimento com um clique.

---

## 👤 Testando como Cliente Final

Para testar o fluxo de **Cliente** (solicitação de test-drive, cancelamento e avaliação pós-teste):

1. Acesse **[http://localhost:5173/registro](http://localhost:5173/registro)**;
2. Cadastre uma conta de cliente informando seus dados e CNH;
3. Escolha qualquer veículo disponível no catálogo e clique em **Agendar Test-Drive**.

---

## 🧪 Executar Testes Unitários

Para rodar a suíte com os 47 testes unitários do backend (JUnit 5 + Mockito):

```bash
cd backend
mvn test
```
