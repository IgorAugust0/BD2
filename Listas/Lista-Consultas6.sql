UPDATE conta
SET saldo_conta=0;
--PRIMEIRO CONFERIMOS COMO ESTá A SITUAÇÃO DAS CONTAS
SELECT *
FROM conta;

--EXECUTAMOS OUTRA CONFERENCIA PARA VER COMO SERãO OS RETORNOS DA PESQUISA
SELECT numero_conta, nome_agencia, nome_cliente, getliquido(numero_conta, nome_agencia, nome_cliente)
FROM conta;

--DEPOIS ATUALIZAMOS ...
UPDATE conta
SET saldo_conta = getliquido(numero_conta, nome_cliente, nome_cliente);

--... E CONFERINDO DE NOVO:
SELECT *
FROM conta
WHERE nome_agencia = 'PUC';

--A DEFINIÇÃO DA FUNÇÃO GETLIQUIDO
CREATE OR REPLACE FUNCTION getliquido(p_numero_conta integer, p_nome_agencia character varying,
                                      p_nome_cliente character varying)
    RETURNS float AS
$BODY$
DECLARE
    saldo_liquido   float;
    soma_deposito   float;
    soma_emprestimo float;
    cursor_relatorio CURSOR FOR SELECT SUM(d.saldo_deposito)   AS total_dep,
                                       SUM(e.valor_emprestimo) AS total_emp
                                FROM conta AS C
                                         NATURAL LEFT OUTER JOIN
                                     (emprestimo AS E NATURAL FULL JOIN deposito AS D)
                                WHERE c.nome_cliente = p_nome_cliente
                                  AND c.nome_agencia = p_nome_agencia
                                  AND c.numero_conta = p_numero_conta
                                GROUP BY c.nome_cliente, c.nome_agencia, c.numero_conta;
BEGIN
    OPEN cursor_relatorio; -- executa a consulta anterior
    saldo_liquido = 0;
    FETCH cursor_relatorio INTO soma_deposito, soma_emprestimo; -- busca na consulta e insere seu conteudo nas variaveis locais inicializadas
    RAISE NOTICE 'O valor de DEP é % e EMP é %', soma_deposito, soma_emprestimo;
    IF FOUND THEN -- se foi encontrado pelo fetch, verifica se os valores sao validos
        IF soma_emprestimo IS NULL THEN soma_emprestimo = 0; END IF; -- simula um coalesce
        saldo_liquido = soma_deposito - soma_emprestimo;
    END IF;
    CLOSE cursor_relatorio;
    RETURN saldo_liquido;
END
$BODY$
    LANGUAGE plpgsql VOLATILE
                     COST 100;
ALTER FUNCTION getliquido(integer, character varying, character varying)
    OWNER TO postgres;
