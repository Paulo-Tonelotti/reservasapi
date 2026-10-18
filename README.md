# Sistema de Reservas

API REST para gerenciamento de reservas de espaços (salas de reunião, auditórios, quadras, etc.), desenvolvida com **Spring Boot** e **JPA/Hibernate**.

Projeto de estudo com foco em regras de negócio reais, transações, controle de concorrência e no ciclo de vida das entidades JPA.

> 🚧 **Status:** em desenvolvimento

## Objetivos de aprendizado

- Mapeamento de entidades e relacionamentos com JPA (`@ManyToOne` com carregamento `LAZY`)
- Ciclo de vida das entidades: *transient*, *managed*, *detached* e *removed* (incluindo *dirty checking*)
- Controle transacional com `@Transactional` (propagação, `readOnly`)
- Consultas personalizadas com JPQL e métodos derivados do Spring Data
- Problema N+1 e carregamento de associações com `@EntityGraph`
- Controle de concorrência com lock pessimista e lock otimista
- Injeção de dependência e organização em camadas (controller, service, repository)
- Tratamento padronizado de erros com `@RestControllerAdvice`

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Data JPA / Hibernate
- Bean Validation
- PostgreSQL 16 (via Docker)
- Maven (wrapper incluso)
- Lombok
- springdoc-openapi (Swagger UI)
- JUnit 5 e Mockito

## Modelo de domínio

| Entidade | Descrição |
|----------|-----------|
| `Cliente` | Pessoa que realiza reservas (nome, e-mail único) |
| `Espaco` | Local que pode ser reservado (nome único, capacidade, valor por hora) |
| `Reserva` | Vínculo entre cliente e espaço em um intervalo de tempo (início, fim, valor total, status) |
| `StatusReserva` | Enum: `PENDENTE`, `CONFIRMADA`, `CANCELADA` |

**Relacionamentos:**

- Uma `Reserva` pertence a um `Cliente` e a um `Espaco` (`@ManyToOne`, `LAZY`)
- Um `Cliente` e um `Espaco` podem ter várias reservas

## Regras de negócio

**Reservas**

- Não é permitido reservar um espaço em um horário que conflite com outra reserva ativa do mesmo espaço
- O início deve ser no futuro, e o fim deve ser posterior ao início
- O cliente e o espaço informados devem existir
- Toda reserva nasce `PENDENTE`
- O valor total é calculado pelo servidor (proporcional aos minutos reservados × valor por hora) e **congelado na reserva**: um reajuste posterior do espaço não altera reservas já feitas
- Transições de status permitidas:

  | Status atual | Confirmar | Cancelar |
  |---|---|---|
  | `PENDENTE` | ✅ | ✅ |
  | `CONFIRMADA` | ❌ | ✅ |
  | `CANCELADA` | ❌ | ❌ |

- Reservas não são apagadas: o cancelamento altera o status para `CANCELADA`, mantém o histórico e libera o horário

**Detecção de conflito:** dois intervalos se sobrepõem quando `inicio < novoFim` **e** `fim > novoInicio`, considerando apenas reservas do mesmo espaço que não estejam canceladas. Uma reserva que termina às 10h não conflita com outra que começa às 10h.

**Clientes e espaços**

- O e-mail do cliente é único e normalizado (sem espaços nas pontas e em minúsculas)
- O nome do espaço é único, sem diferenciar maiúsculas e minúsculas
- Um cliente ou espaço só pode ser excluído se não tiver reservas vinculadas

## Controle de concorrência

| Problema | Solução | Comportamento |
|---|---|---|
| Duas requisições tentando reservar o mesmo horário ao mesmo tempo | **Lock pessimista** (`PESSIMISTIC_WRITE`) na linha do espaço durante a criação da reserva | a segunda requisição espera a primeira terminar e então recebe `409` |
| Confirmar e cancelar a mesma reserva ao mesmo tempo | **Lock otimista** (`@Version` na entidade `Reserva`) | a segunda alteração é recusada no commit com `409` |

## Como executar

### Pré-requisitos

- JDK 21
- Docker

### 1. Subir o banco de dados

```bash
docker compose up -d
```

O PostgreSQL fica disponível na porta **5435** do host (veja o `docker-compose.yml`).

### 2. Configurar as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto (ele está no `.gitignore` e não deve ser versionado):

```properties
DB_HOST=localhost
DB_PORT=5435
DB_NAME=reservas
DB_USER=postgres
DB_PASSWORD=postgres
```

A aplicação lê esse arquivo na inicialização (`spring.config.import`). Variáveis de ambiente do sistema com os mesmos nomes também funcionam.

### 3. Executar a aplicação

```bash
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8084`.

A documentação interativa (Swagger UI) fica em `http://localhost:8084/swagger-ui.html`, e a especificação OpenAPI em `http://localhost:8084/v3/api-docs`.

As tabelas são criadas automaticamente pelo Hibernate (`ddl-auto: update`).

### Executar os testes

```bash
./mvnw test
```

O teste `ReservasapiApplicationTests` sobe a aplicação completa e precisa do banco rodando e do `.env` configurado.

## Endpoints

### Clientes

| Método | Rota | Descrição | Sucesso |
|--------|------|-----------|---------|
| `POST` | `/clientes` | Cadastrar cliente | `201` |
| `GET` | `/clientes` | Listar clientes (paginado) | `200` |
| `GET` | `/clientes/{id}` | Buscar cliente por id | `200` |
| `PUT` | `/clientes/{id}` | Atualizar cliente | `200` |
| `DELETE` | `/clientes/{id}` | Excluir cliente (sem reservas) | `204` |

### Espaços

| Método | Rota | Descrição | Sucesso |
|--------|------|-----------|---------|
| `POST` | `/espacos` | Cadastrar espaço | `201` |
| `GET` | `/espacos` | Listar espaços (paginado) | `200` |
| `GET` | `/espacos/{id}` | Buscar espaço por id | `200` |
| `PUT` | `/espacos/{id}` | Atualizar espaço | `200` |
| `DELETE` | `/espacos/{id}` | Excluir espaço (sem reservas) | `204` |

### Reservas

| Método | Rota | Descrição | Sucesso |
|--------|------|-----------|---------|
| `POST` | `/reservas` | Criar reserva (valida conflito de horário) | `201` |
| `GET` | `/reservas` | Listar reservas (paginado, com filtros) | `200` |
| `GET` | `/reservas/{id}` | Buscar reserva por id | `200` |
| `PATCH` | `/reservas/{id}/confirmar` | Confirmar reserva | `200` |
| `PATCH` | `/reservas/{id}/cancelar` | Cancelar reserva | `200` |

**Filtros da listagem de reservas** (todos opcionais e combináveis): `espacoId`, `clienteId` e `status`.

```
GET /reservas?espacoId={uuid}&status=CONFIRMADA&sort=inicio,desc
```

### Paginação

As listagens aceitam os parâmetros `page` (começa em 0), `size` (máximo 50) e `sort` (`campo,asc` ou `campo,desc`):

```
GET /clientes?page=0&size=10&sort=nome,asc
```

A resposta segue o formato:

```json
{
  "content": [ ... ],
  "page": { "size": 10, "number": 0, "totalElements": 42, "totalPages": 5 }
}
```

### Exemplo: criar reserva

```http
POST /reservas
Content-Type: application/json

{
  "clienteId": "b0f103f2-1528-4d41-8c0d-189fd5d84982",
  "espacoId": "210161ef-ace5-44c0-866e-4364831c72be",
  "inicio": "2026-12-01T10:00:00",
  "fim": "2026-12-01T12:00:00"
}
```

## Tratamento de erros

Todas as respostas de erro seguem o mesmo formato:

```json
{
  "timestamp": "2026-10-17T17:12:05.385Z",
  "status": 409,
  "erro": "Operação não permitida",
  "mensagem": "O horário informado já está ocupado",
  "caminho": "/reservas"
}
```

Erros de validação incluem também a lista de campos inválidos (`erros`).

| Status | Quando |
|--------|--------|
| `400` | Dados inválidos, JSON mal formatado ou parâmetro com tipo incorreto |
| `404` | Cliente, espaço ou reserva não encontrado |
| `409` | Conflito de horário, e-mail ou nome duplicado, transição de status inválida, exclusão com reservas vinculadas ou alteração concorrente |

## Dados de teste

A pasta `dados-teste/` contém arquivos CSV e JSON para popular a API e testar casos de borda pelo *Collection Runner* do Insomnia (ou ferramenta similar). Cada arquivo de casos de erro traz as colunas `caso` e `esperado` (status HTTP esperado).

## Roadmap

- [X] Entidades e relacionamentos
- [X] CRUD de clientes e espaços
- [X] Criação de reserva com validação de conflito
- [X] Confirmação e cancelamento de reserva
- [X] Tratamento global de erros (`@RestControllerAdvice`)
- [X] Paginação e filtros na listagem
- [X] Lock pessimista na criação de reservas
- [X] Controle de concorrência com `@Version` (optimistic locking)
- [X] Valor total calculado e congelado na reserva
- [X] Testes unitários do `ReservaService` (Mockito)
- [X] Nome do cliente e do espaço na resposta da reserva, sem N+1 (`@EntityGraph`)
- [X] Documentação da API com Swagger (springdoc-openapi)
- [ ] Remarcação de reservas
- [ ] Migrações de banco com Flyway
- [ ] Testes de integração das consultas JPQL (`@DataJpaTest` + Testcontainers)

## Autor

Feito por **Paulo Tonelotti** como projeto de estudo.
