DROP DATABASE IF EXISTS db_lavacao;
CREATE DATABASE db_lavacao;
USE db_lavacao;

CREATE TABLE cor
(
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    nome varchar(25) not null UNIQUE,
    CONSTRAINT pk_cor
        PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE marca
(
    id   INT         NOT NULL AUTO_INCREMENT,
    nome varchar(25) not null unique,
    CONSTRAINT pk_marca
        PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE servico
(
    id        INT                                                 NOT NULL AUTO_INCREMENT,
    descricao VARCHAR(100)                                        NOT NULL,
    valor     DECIMAL                                             NOT NULL,
    categoria ENUM ('PEQUENO','MEDIO','GRANDE', 'MOTO', 'PADRAO') NOT NULL DEFAULT 'PADRAO',
    CONSTRAINT pk_servico
        PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE parametros_de_sistema
(
    chave  char(6) not null,
    pontos int     not null,
    constraint pk_parametros_de_sistema
        primary key (chave)
) engine = InnoDB;

CREATE TABLE modelo
(
    id        INT                                                 NOT NULL AUTO_INCREMENT,
    descricao VARCHAR(100)                                        NOT NULL unique,
    categoria ENUM ('PEQUENO','MEDIO','GRANDE', 'MOTO', 'PADRAO') NOT NULL DEFAULT 'PADRAO',
    marca_id  INT                                                 NOT NULL,
    CONSTRAINT pk_modelo
        PRIMARY KEY (id),
    CONSTRAINT fk_modelo_marca
        foreign key (marca_id) references marca (id) on update cascade
) engine = InnoDB;

CREATE TABLE motor
(
    id_modelo        int                                                      not null,
    potencia         int                                                      not null,
    tipo_combustivel ENUM ('GASOLINA','ETANOL','FLEX','DIESEL','GNV','OUTRO') not null,
    CONSTRAINT pk_motor
        PRIMARY KEY (id_modelo),
    CONSTRAINT fk_motor_modelo
        foreign key (id_modelo) references modelo (id) on delete cascade on update cascade
) engine = InnoDB;

CREATE TABLE cliente
(
    id            INT          NOT NULL AUTO_INCREMENT,
    nome          VARCHAR(100) NOT NULL,
    celular       varchar(20)  NOT NULL,
    email         varchar(50) unique,
    data_cadastro date,
    CONSTRAINT pk_cliente
        PRIMARY KEY (id)
) engine = InnoDB;

CREATE TABLE pessoa_fisica
(
    id_cliente      INT      NOT NULL,
    cpf             CHAR(11) NOT NULL unique,
    data_nascimento date,
    CONSTRAINT pk_pessoa_fisica
        PRIMARY KEY (id_cliente),
    CONSTRAINT fk_pessoa_fisica_cliente
        foreign key (id_cliente) references cliente (id) on delete cascade on update cascade
) engine = InnoDB;

CREATE TABLE pessoa_juridica
(
    id_cliente         INT      NOT NULL,
    cnpj               CHAR(14) NOT NULL unique,
    inscricao_estadual varchar(20),
    CONSTRAINT pk_pessoa_juridica PRIMARY KEY (id_cliente),
    CONSTRAINT fk_pessoa_juridica_cliente
        foreign key (id_cliente) references cliente (id) on delete cascade on update cascade
) engine = InnoDB;

CREATE TABLE pontuacao
(
    id_cliente INT NOT NULL,
    quantidade int not null default 0,
    CONSTRAINT pk_pontuacao PRIMARY KEY (id_cliente),
    CONSTRAINT fk_pontuacao_cliente
        foreign key (id_cliente) references cliente (id) on delete cascade on update cascade
) engine = InnoDB;

CREATE TABLE veiculo
(
    id          int         not null auto_increment,
    placa       varchar(20) not null,
    observacoes varchar(200),
    id_cliente  int         not null,
    id_cor      bigint      not null,
    id_modelo   int         not null,
    constraint pk_veiculo primary key (id),
    CONSTRAINT fk_veiculo_cliente
        foreign key (id_cliente) references cliente (id),
    CONSTRAINT fk_veiculo_cor
        foreign key (id_cor) references cor (id),
    CONSTRAINT fk_veiculo_modelo
        foreign key (id_modelo) references modelo (id)
) engine = InnoDB;

CREATE TABLE ordem_servico
(
    numero     bigint                                not null auto_increment,
    total      double                                not null,
    agenda     date                                  not null,
    desconto   double                                not null default 0,
    status     ENUM ('ABERTA','FECHADA','CANCELADA') NOT NULL DEFAULT 'ABERTA',
    id_veiculo INT                                   NOT NULL,
    constraint pk_ordem_servico primary key (numero),
    CONSTRAINT fk_ordem_servico_veiculo
        FOREIGN KEY (id_veiculo) REFERENCES veiculo (id)
) engine = InnoDB;

CREATE TABLE item_os
(
    numero_os     bigint not null,
    id_servico    int    not null,
    observacoes   varchar(100),
    valor_servico double not null,
    constraint pk_item_os
        primary key (numero_os, id_servico),
    CONSTRAINT fk_item_os_ordem_servico
        FOREIGN KEY (numero_os)
            REFERENCES ordem_servico (numero) on delete cascade on update cascade,
    CONSTRAINT fk_item_os_servico
        FOREIGN KEY (id_servico) REFERENCES servico (id) on delete cascade on update cascade
) engine = InnoDB;

-- ==========================
-- COR
-- ==========================
INSERT INTO cor (nome)
VALUES ('Branco'),
       ('Preto'),
       ('Prata'),
       ('Vermelho'),
       ('Azul'),
       ('Cinza');

-- ==========================
-- MARCA
-- ==========================
INSERT INTO marca (nome)
VALUES ('Volkswagen'),
       ('Fiat'),
       ('Chevrolet'),
       ('Toyota'),
       ('Honda'),
       ('Yamaha');

-- ==========================
-- SERVIÇOS
-- ==========================
INSERT INTO servico (descricao, valor, categoria)
VALUES ('Lavagem Simples', 40.00, 'PADRAO'),
       ('Lavagem Completa', 65.00, 'PADRAO'),
       ('Polimento', 150.00, 'PADRAO'),
       ('Enceramento', 80.00, 'PADRAO'),
       ('Higienização Interna', 120.00, 'PADRAO'),
       ('Lavagem do Motor', 60.00, 'PADRAO'),
-- PEQUENO
       ('Vitrificação de Pintura Compacto', 450.00, 'PEQUENO'),
       ('Lavagem Técnica Compacto', 95.00, 'PEQUENO'),
       ('Higienização Premium Compacto', 180.00, 'PEQUENO'),

-- MÉDIO
       ('Vitrificação de Pintura Médio', 550.00, 'MEDIO'),
       ('Polimento Técnico Médio', 280.00, 'MEDIO'),
       ('Lavagem Premium Médio', 110.00, 'MEDIO'),

-- GRANDE
       ('Lavagem de Caminhonete/SUV', 140.00, 'GRANDE'),
       ('Polimento Completo SUV', 380.00, 'GRANDE'),
       ('Higienização Premium SUV', 250.00, 'GRANDE'),

-- MOTO
       ('Lavagem Detalhada de Moto', 50.00, 'MOTO'),
       ('Polimento de Tanque e Carenagem', 90.00, 'MOTO'),
       ('Lubrificação e Limpeza da Corrente', 35.00, 'MOTO');

-- ==========================
-- MODELOS
-- ==========================
INSERT INTO modelo (descricao, categoria, marca_id)
VALUES ('Gol', 'PEQUENO', 1),
       ('Polo', 'MEDIO', 1),
       ('Uno', 'PEQUENO', 2),
       ('Toro', 'GRANDE', 2),
       ('Onix', 'MEDIO', 3),
       ('Corolla', 'MEDIO', 4),
       ('Civic', 'MEDIO', 5),
       ('Factor 150', 'MOTO', 6);

-- ==========================
-- MOTORES
-- ==========================
INSERT INTO motor
VALUES (1, 84, 'FLEX'),
       (2, 128, 'FLEX'),
       (3, 75, 'FLEX'),
       (4, 185, 'DIESEL'),
       (5, 116, 'FLEX'),
       (6, 177, 'FLEX'),
       (7, 155, 'FLEX'),
       (8, 12, 'GASOLINA');

-- ==========================
-- CLIENTES
-- ==========================
INSERT INTO cliente (nome, celular, email, data_cadastro)
VALUES ('Ricardo', '48999990001', 'paulo@email.com', CURDATE()),
       ('Maria Oliveira', '48999990002', 'maria@email.com', CURDATE()),
       ('João Souza', '48999990003', 'joao@email.com', CURDATE()),
       ('Mercado Central', '48999990004', 'mercado@email.com', CURDATE()),
       ('Transportadora Sul', '48999990005', 'transportadora@email.com', CURDATE());

-- ==========================
-- PESSOA FÍSICA
-- ==========================
INSERT INTO pessoa_fisica
VALUES (1, '11111111111', '2000-05-10'),
       (2, '22222222222', '1998-08-20'),
       (3, '33333333333', '1990-01-15');

-- ==========================
-- PESSOA JURÍDICA
-- ==========================
INSERT INTO pessoa_juridica
VALUES (4, '11111111000111', '123456'),
       (5, '22222222000122', '654321');

-- ==========================
-- PONTUAÇÃO
-- ==========================
INSERT INTO pontuacao
VALUES (1, 0),
       (2, 0),
       (3, 0),
       (4, 0),
       (5, 0);

-- ==========================
-- VEÍCULOS
-- ==========================
INSERT INTO veiculo (placa, observacoes, id_cliente, id_cor, id_modelo)
VALUES ('ABC1A11', NULL, 1, 1, 1),
       ('DEF2B22', 'Possui película', 2, 2, 5),
       ('GHI3C33', NULL, 3, 3, 6),
       ('JKL4D44', 'Veículo da empresa', 4, 4, 4),
       ('MNO5E55', NULL, 5, 5, 8);

-- ==========================
-- ORDENS DE SERVIÇO
-- ==========================
INSERT INTO ordem_servico
    (total, agenda, desconto, status, id_veiculo)
VALUES (40, '2026-07-01', 0, 'ABERTA', 1);

-- ==========================
-- ITENS DAS ORDENS
-- ==========================
INSERT INTO item_os
    (numero_os, id_servico, observacoes, valor_servico)
VALUES (1, 1, 'Veículo com muita poeira e insetos na parte frontal.', 40.00);

insert into parametros_de_sistema(chave, pontos)
values ('pontos', 0);

