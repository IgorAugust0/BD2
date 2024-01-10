--Funções de agregação:

--Encontre a soma total de depósitos para cada cliente

SELECT nome_cliente, SUM(saldo_deposito)
FROM deposito
GROUP BY nome_cliente;

--Encontre a soma total de depósitos para cada cliente
--e ordene pela ordem descendente dos depósitos

SELECT nome_cliente, SUM(saldo_deposito)
FROM deposito
GROUP BY nome_cliente
ORDER BY SUM(saldo_deposito) DESC;

--Conte quantos depósitos realizou cada cliente

SELECT nome_cliente, COUNT(1) -- count(*) / count(numero_deposito)
FROM deposito
GROUP BY nome_cliente;

--Conte quantos depósitos realizou cada cliente e ordene
--pelo ordem descendente da quantidade e depositos

SELECT nome_cliente, COUNT(1) -- count(*) / count(numero_deposito)
FROM deposito
GROUP BY nome_cliente
ORDER BY COUNT(1) DESC;

--Encontre a soma total de depósitos para cada cliente,
--e também a quantidade de depósitos. Mostre o resultado
--ordenado primeiro pela ordem ascendente da quantidade de depósitos
--e depois pela soma total descendente de depósitos de cada cliente

SELECT nome_cliente, SUM(saldo_deposito), COUNT(1)
FROM deposito
GROUP BY nome_cliente
ORDER BY COUNT(1), SUM(saldo_deposito) DESC;

--Encontre o número de depositantes em cada agência
SELECT nome_agencia, COUNT(nome_cliente)
FROM deposito
GROUP BY nome_agencia
ORDER BY COUNT(nome_cliente) DESC;

--Mas a cláusula anterior está errada porque um cliente
--pode fazer mais de um depósito por agencia. Veja:
SELECT nome_agencia, nome_cliente, COUNT(nome_cliente)
FROM deposito
GROUP BY nome_agencia, nome_cliente;

--Solução:
SELECT nome_agencia, COUNT(DISTINCT nome_cliente)
FROM deposito
GROUP BY nome_agencia
ORDER BY COUNT(nome_cliente) DESC;

--Encontre o saldo médio de depósitos de cada agência

SELECT nome_agencia, ROUND(AVG(saldo_deposito)) --arredondar (sem casas decimais)
FROM deposito
GROUP BY nome_agencia;

--Encontre o saldo médio de depósitos de cada agência
--mas mostre apenas as agencias e saldo médio que forem
--maiores do que R$ 1.200,00

SELECT nome_agencia,
       ROUND(AVG(saldo_deposito)::numeric, 2) --arredondar para duas casas, cast para numeric para especificar as casas
FROM deposito
GROUP BY nome_agencia
HAVING AVG(saldo_deposito) > 1200;

--Selecione o valor do maior depósito

SELECT MAX(saldo_deposito)
FROM deposito;

--Selecione o valor do maior, da média e do menor depósito

SELECT MAX(saldo_deposito) AS maior, ROUND(AVG(saldo_deposito)::numeric, 2) AS media, MIN(saldo_deposito) AS menor
FROM deposito;

--Selecione o valor do maior, da média
--e do menor depósito, todos por agencia

SELECT nome_agencia                           AS agencia,
       MAX(saldo_deposito)                    AS maior,
       ROUND(AVG(saldo_deposito)::numeric, 2) AS media,
       MIN(saldo_deposito)                    AS menor
FROM deposito
GROUP BY nome_agencia;


--Selecione o nome do cliente que fez o maior deposito

SELECT nome_cliente, saldo_deposito
FROM deposito
WHERE saldo_deposito = (SELECT MAX(saldo_deposito) FROM deposito)
