# Sistema de Reservas

API REST para gerenciamento de reservas de espaços (hotel, salão, quadra esportiva, etc.), desenvolvida com **Spring Boot** e **JPA/Hibernate**.

Projeto de estudo com foco em regras de negócio reais, transações e no ciclo de vida das entidades JPA.

> 🚧 **Status:** em desenvolvimento

## Objetivos de aprendizado

- Mapeamento de entidades e relacionamentos com JPA (`@ManyToOne`, `@OneToMany`)
- Ciclo de vida das entidades: *transient*, *managed*, *detached* e *removed*
- Controle transacional com `@Transactional`
- Consultas personalizadas com JPQL
- Injeção de dependência e organização em camadas (controller, service, repository)
- Tratamento padronizado de erros com `@RestControllerAdvice`

## Tecnologias

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- Banco de dados relacional (definir: H2, PostgreSQL, MySQL...)
- Maven ou Gradle (definir)

## Modelo de domínio

| Entidade | Descrição |
|----------|-----------|
| `Cliente` | Pessoa que realiza reservas (nome, e-mail) |
| `Espaco` | Local que pode ser reservado (nome, capacidade, valor por hora) |
| `Reserva` | Vínculo entre cliente e espaço em um intervalo de tempo (início, fim, status) |
| `StatusReserva` | Enum: `PENDENTE`, `CONFIRMADA`, `CANCELADA` |

**Relacionamentos:**

- Uma `Reserva` pertence a um `Cliente` e a um `Espaco`
- Um `Cliente` e um `Espaco` podem ter várias reservas

## Regras de negócio

- Não é permitido reservar um espaço em um horário que conflite com outra reserva ativa
- O horário de fim deve ser posterior ao de início
- O cliente e o espaço informados devem existir
- Reservas não são apagadas: o cancelamento altera o status para `CANCELADA`, mantém o histórico e libera o horário

**Detecção de conflito:** dois intervalos se sobrepõem quando `inicio < novoFim` **e** `fim > novoInicio`, considerando apenas reservas do mesmo espaço que não estejam canceladas.

## Como executar

### Pré-requisitos

- JDK instalado (versão a definir)
- Maven ou Gradle

### Passos

```bash
# Clonar o repositório
git clone <url-do-repositorio>
cd <nome-do-projeto>

# Executar a aplicação
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

## Endpoints (planejados)

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/clientes` | Cadastrar cliente |
| `GET` | `/clientes` | Listar clientes |
| `POST` | `/espacos` | Cadastrar espaço |
| `GET` | `/espacos` | Listar espaços |
| `POST` | `/reservas` | Criar reserva (valida conflito de horário) |
| `GET` | `/reservas` | Listar reservas |
| `PATCH` | `/reservas/{id}/cancelar` | Cancelar reserva |

Em caso de conflito de horário, a API responde com `409 Conflict`.

## Roadmap

- [ ] Entidades e relacionamentos
- [ ] CRUD de clientes e espaços
- [ ] Criação de reserva com validação de conflito
- [ ] Cancelamento de reserva
- [ ] Tratamento global de erros (`@RestControllerAdvice`)
- [ ] Paginação e filtros na listagem
- [ ] Testes demonstrando o ciclo de vida das entidades
- [ ] Controle de concorrência com `@Version` (optimistic locking)

## Autor

Feito por **<seu-nome>** como projeto de estudo.