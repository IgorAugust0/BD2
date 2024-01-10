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
    FETCH cursor_relatorio INTO soma_deposito, soma_emprestimo;
    -- busca na consulta e insere seu conteudo nas variaveis locais inicializadas
    --RAISE NOTICE 'O valor de DEP é % e EMP é %', soma_deposito, soma_emprestimo;
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
    OWNER TO aluno;

--DEFINININDO A FUNÇÃO A SER ACIONADA POR TRIGGER (GATILHO)
CREATE OR REPLACE FUNCTION Atualizar_Ativos_F1()
    RETURNS trigger AS
$BODY$
DECLARE
    l_ativo_agencia float;
    l_nome_agencia  character varying;
    cursor_relatorio CURSOR FOR SELECT nome_agencia, SUM(saldo_conta) --, coalesce(s    um(saldo_conta))
                                FROM CONTA
                                GROUP BY nome_agencia;
BEGIN
    RAISE NOTICE 'FUNÇÃO QUE NÃO RECEBE ARGUMENTOS';
    OPEN cursor_relatorio;
    LOOP
        FETCH cursor_relatorio INTO l_ativo_agencia;
        IF FOUND THEN
            IF l_ativo_agencia IS NULL THEN l_ativo_agencia = 0; END IF;
            UPDATE agencia SET ativo_agencia = l_ativo_agencia WHERE nome_agencia = l_nome_agencia;
        END IF;
        IF NOT FOUND THEN EXIT; END IF;
    END LOOP;
    CLOSE cursor_relatorio;
    RETURN NULL;
END
$BODY$
    LANGUAGE plpgsql VOLATILE
                     COST 100;
ALTER FUNCTION Atualizar_Ativos_F1() OWNER TO aluno;

--CRIANDO O GATILHO
CREATE TRIGGER TRIGGER_Atualiza_Ativos_F1
    AFTER UPDATE
    ON CONTA
    FOR EACH STATEMENT
EXECUTE PROCEDURE Atualizar_Ativos_F1();
-- DROP TRIGGER trigger_atualiza_ativos_f2 ON conta;

--INICIALMENTE OS ATIVOS DA AGENCIA ESTÃO COM VALOR ZERO

UPDATE AGENCIA
SET ativo_agencia = 0;

SELECT *
FROM AGENCIA;

SELECT *
FROM CONTA;

--AO ATUALIZARMOS OS SALDOS DAS CONTAS, DISPARAMOS A ATUALIZAÇÃO DOS ATIVOS DAS AGÊNCIAS
UPDATE CONTA
SET saldo_conta=getliquido(numero_conta, nome_agencia, nome_cliente);

--DEFINININDO A NOVA FUNÇÃO A SER ACIONADA POR TRIGGER (GATILHO)
CREATE OR REPLACE FUNCTION Atualizar_Ativos_F2()
    RETURNS trigger AS
$BODY$
DECLARE
    l_ativo_agencia float;
    l_nome_agencia  character varying;
    cursor_relatorio CURSOR FOR SELECT nome_agencia, SUM(saldo_conta)
                                FROM CONTA
                                GROUP BY nome_agencia;
BEGIN
    --RAISE NOTICE 'FUNÇÃO QUE RECEBE ARGUMENTO %', TG_ARGV[0];
    OPEN cursor_relatorio;
    LOOP
        FETCH cursor_relatorio INTO l_nome_agencia;
        IF FOUND THEN
            IF l_ativo_agencia IS NULL THEN l_ativo_agencia = 0; END IF;
            UPDATE agencia SET ativo_agencia = l_ativo_agencia WHERE nome_agencia = l_nome_agencia;
        END IF;
        IF NOT FOUND THEN EXIT; END IF;
    END LOOP;
    CLOSE cursor_relatorio;
    RETURN NULL;
END
$BODY$
    LANGUAGE plpgsql VOLATILE
                     COST 100;
ALTER FUNCTION Atualizar_Ativos_F2() OWNER TO aluno;

--CRIANDO UMA VARIAÇÃO DO GATILHO
-- DROP TRIGGER trigger_atualiza_ativos_f1 ON conta;
CREATE TRIGGER TRIGGER_Atualiza_Ativos_F2
    AFTER UPDATE
    ON conta
    FOR EACH STATEMENT
EXECUTE PROCEDURE Atualizar_Ativos_F2('CONSTANTE');

--INICIALMENTE OS ATIVOS DA AGENCIA ESTÃO COM VALOR ZERO
UPDATE AGENCIA
SET ativo_agencia = 0;

SELECT *
FROM AGENCIA;

SELECT *
FROM CONTA;

--AO ATUALIZARMOS OS SALDOS DAS CONTAS, DISPARAMOS A ATUALIZAÇÃO DOS ATIVOS DAS AGÊNCIAS
UPDATE CONTA
SET saldo_conta = getliquido(numero_conta, nome_agencia, nome_cliente);
