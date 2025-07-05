# PA Web Server

Servidor Web paralelo desenvolvido em Java para a disciplina de Programação Avançada.

## Descrição do Projeto

O **pa-web-server** é um servidor web multi-threaded que serve páginas HTML estáticas de forma paralela, implementando conceitos avançados de programação concorrente em Java. O projeto demonstra o uso de threads, semáforos, trancas e padrões arquiteturais para desenvolvimento paralelo.

## Funcionalidades Principais

- Servir páginas HTML estáticas de diretórios e subdiretórios
- Processamento paralelo de até 5 pedidos simultâneos
- Controlo de acesso exclusivo por documento (First-Come, First-Served)
- Sistema de logging assíncrono em formato JSON
- Configuração através de ficheiro `server.config`
- Retorno automático de `index.html` para diretórios
- Tratamento de erros 404 para recursos inexistentes

## Tecnologias Utilizadas

- **Java** - Linguagem principal
- **Threads/Runnables** - Processamento paralelo
- **Semáforos** - Controlo de concorrência
- **Trancas (Locks)** - Sincronização de recursos
- **Maven** - Gestão de dependências
- **JaCoCo** - Relatórios de cobertura de código
- **JUnit Jupiter** - Testes unitários

## Pré-requisitos

- Java JDK 17 ou superior
- Maven 3.6+

## Como Executar

- Abrir o projeto no IDE
- Executar a classe `Main.java`

## Como Visualizar

Após a execução, abra o browser e aceda a [http://localhost:8888]
