--O cliente "Thiago Andrade Fiuza", ANTES MORADOR DA CIDADE DE ITAMBACURI, MUDOU DE ENDEREÇO
--E SOLICITOU A ALTERÇÃO DE SEUS DADOS NO BANCO. O NOVO ENDEREÇO PASSA A SER NA CIDADE DE
--UBERLANDIA, À AVENIDA AFONSO PENA. ATUALIZE OS DADOS DO cliente:

--ANTES
SELECT *
FROM cliente
WHERE cidade_cliente = 'Itambacuri';

UPDATE cliente
SET cidade_cliente = 'Uberlandia',
    rua_cliente='Avenida Afonso Pena'
WHERE nome_cliente = 'Thiago Andrade Fiuza'
  AND cidade_cliente = 'Itambacuri';

--DEPOIS
SELECT *
FROM cliente
WHERE cidade_cliente = 'Uberlandia';

--AO FINAL DO MÊS É NECESSÁRIO ATUALIZAR O SALDO DAS CONTAS DOS clienteS APLICANDO
--A CORREÇÃO DA INFLAÇÃO DE 1% SOBRE AS MESMAS. IMPLEMENTE ESTA ATUALIZAÇÃO VIA SQL.

--ANTES E DEPOIS
SELECT nome_cliente, SUM(saldo_deposito) AS soma
FROM deposito
GROUP BY nome_cliente
ORDER BY soma;

UPDATE deposito
SET saldo_deposito = saldo_deposito * 1.01;

--AO FINAL DO MÊS É NECESSÁRIO ATUALIZAR O SALDO DAS CONTAS DOS clienteS APLICANDO
--OS JUROS DOS INVESTIMENTOS SOBRE AS MESMAS.
--clienteS COM SALDO ATÉ DEZ MIL RECEBEM 3% DE ACRÉSCIMO.
--clienteS COM SALDO MAIOR QUE DEZ MIL RECEBEM 5% DE ACRÉSCIMO.

--ANTES E DEPOIS
SELECT nome_cliente, SUM(saldo_deposito) AS soma
FROM deposito
GROUP BY nome_cliente
ORDER BY soma;

UPDATE deposito
SET saldo_deposito = saldo_deposito * 1.03
WHERE saldo_deposito <= 10000;

UPDATE deposito
SET saldo_deposito = saldo_deposito * 1.05
WHERE saldo_deposito > 10000;

--A ORDEM DE EXECUÇÃO DAS CONSULTAS É IMPORTANTE PORQUE SENÃO UM
--cliente COM SALDO LIGEIRAMENTE INFERIOR A DEZ MIL PODE RECEBER
--UM ACRÉSCIMO DE 8.15% = (1.03*1.05)

--INSERIR NOVOS DEPOSITOS NO BANCO DE DADOS

INSERT INTO deposito -- (numero_deposito, numero_conta, nome_agencia, nome_cliente, saldo_deposito)
VALUES (10000, 20000, 'PUC', 'Carlos Eduardo', 1200);

--INCLUIR PARA TODOS OS clienteS COM EMPRESTIMOS NA AGENCIA PUC
--UMA CONTA DE DEPÓSITO NO VALOR DE R$ 200,00.

INSERT INTO deposito
SELECT numero_emprestimo, numero_conta, nome_agencia, nome_cliente, 200
FROM emprestimo
WHERE nome_agencia = 'PUC';

--VERIFICANDO:
SELECT *
FROM deposito
WHERE nome_agencia = 'PUC'
  AND saldo_deposito = 200;

--O MAIS CORRETO AQUI É A CRIAÇÃO DE UM NÚMERO SEQUENCIAL PARA
--IDENTIFICAR O NÚMERO DO DEPÓSITO. SUPONDO QUE JÁ TEMOS
--ALGUNS NÚMEROS TEMOS QUE COMEÇAR A NOSSA SEQUENCIA DE MAX+1

SELECT MAX(numero_deposito)
FROM deposito;

CREATE SEQUENCE seq_deposito INCREMENT 1 START 8795331;
--              ^^^^^^^^^^^^
--use letras minúsculas no nome da sequencia para evitar problemas

--AGORA NÃO PRECISAMOS INSERIR O NÚMERO DO DEPÓSITO MANUALMENTE

INSERT INTO deposito
SELECT ('seq_deposito'), numero_conta, nome_agencia, nome_cliente, 200
FROM emprestimo
WHERE nome_agencia = 'PUC';

--07- UM MILIONÁRIO DECIDIU DOAR PARTE DE SUA FORTUNA PARA clienteS
--DO BANCO COM DÍVIDAS ALTAS. O CRITÉRIO SERÁ DEPOSITAR 2 MIL REAIS
--PARA TODOS OS clienteS DO BANCO QUE FIZERAM EMPRESTIMOS CUJAS
--SOMAS ULTRAPASSEM A SOMA DE DEPÓSITOS. CRIE UM SQL PARA INSERIR
-- NA TABELA DE DEPÓSITOS QUANTIAS DE 2 MIL REAIS PARA TODAS AS CONTAS
-- QUE ESTÃO COM SALDO NEGATIVO EM MAIS DE DOIS MIL REAIS.

INSERT INTO deposito
SELECT NEXTVAL('seq_deposito'), numero_conta, nome_agencia, nome_cliente, 2000
FROM (SELECT numero_conta, nome_agencia, nome_cliente
      FROM (SELECT numero_conta, nome_agencia, nome_cliente, SUM(valor_emprestimo) * (-1) AS soma
            FROM emprestimo
            GROUP BY numero_conta, nome_agencia, nome_cliente
            UNION
            SELECT numero_conta, nome_agencia, nome_cliente, SUM(saldo_deposito) AS soma
            FROM deposito
            GROUP BY numero_conta, nome_agencia, nome_cliente) AS relatorio1
      GROUP BY numero_conta, nome_agencia, nome_cliente
      HAVING SUM(soma) < -2000) AS RELATORIO2;

--VALORES NEGATIVOS

SELECT numero_conta, nome_agencia, nome_cliente, SUM(valor_emprestimo) * (-1) AS soma
FROM emprestimo
GROUP BY numero_conta, nome_agencia, nome_cliente;

--VALORES POSITIVOS

SELECT numero_conta, nome_agencia, nome_cliente, SUM(saldo_deposito) AS soma
FROM deposito
GROUP BY numero_conta, nome_agencia, nome_cliente;

--JUNÇÃO DOS DADOS NEGATIVOS E POSITIVOS
SELECT numero_conta, nome_agencia, nome_cliente
FROM (SELECT numero_conta, nome_agencia, nome_cliente, SUM(valor_emprestimo) * (-1) AS soma
      FROM emprestimo
      GROUP BY numero_conta, nome_agencia, nome_cliente
      UNION
      SELECT numero_conta, nome_agencia, nome_cliente, SUM(saldo_deposito) AS soma
      FROM deposito
      GROUP BY numero_conta, nome_agencia, nome_cliente) AS relatorio1;
--GROUP BY numero_conta, nome_agencia, nome_cliente
--HAVING SUM(soma) < -2000;

--JUNÇÃO DOS DADOS NEGATIVOS E POSITIVOS E FILTRAGEM DOS REGISTROS

SELECT numero_conta, nome_agencia, nome_cliente
FROM (SELECT numero_conta, nome_agencia, nome_cliente, SUM(VALOR_EMPRESTIMO) * (-1) AS soma
      FROM emprestimo
      GROUP BY numero_conta, nome_agencia, nome_cliente
      INTERSECT
      SELECT numero_conta, nome_agencia, nome_cliente, SUM(saldo_deposito) AS soma
      FROM deposito
      GROUP BY numero_conta, nome_agencia, nome_cliente) AS relatorio1
GROUP BY numero_conta, nome_agencia, nome_cliente
HAVING SUM(soma) < -2000;

--ENTÃO:
INSERT INTO deposito
SELECT NEXTVAL('seq_deposito'), numero_conta, nome_agencia, nome_cliente, 2000
FROM (SELECT numero_conta, nome_agencia, nome_cliente
      FROM (SELECT numero_conta, nome_agencia, nome_cliente, SUM(valor_emprestimo) * (-1) AS soma
            FROM emprestimo
            GROUP BY numero_conta, nome_agencia, nome_cliente
            UNION
            SELECT numero_conta, nome_agencia, nome_cliente, SUM(saldo_deposito) AS soma
            FROM deposito
            GROUP BY numero_conta, nome_agencia, nome_cliente) AS relatorio1
      GROUP BY numero_conta, nome_agencia, nome_cliente
      HAVING SUM(soma) < -2000) AS relatorio2;
