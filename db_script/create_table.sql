USE TESTDB;
GO

IF OBJECT_ID(N'dbo.t_trxn_card_payments', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.t_trxn_card_payments (
        id bigint IDENTITY(1,1) NOT NULL,
        merchant_reference nvarchar(80) NOT NULL,
        amount decimal(19,2) NOT NULL,
        currency nvarchar(3) NOT NULL,
        card_token nvarchar(255) NOT NULL,
        masked_card_number nvarchar(19) NOT NULL,
        status nvarchar(20) NOT NULL,
        created_at datetime2(6) NOT NULL,
        updated_at datetime2(6) NOT NULL,
        version bigint NULL,

        CONSTRAINT PK_t_trxn_card_payments PRIMARY KEY (id),
        CONSTRAINT UQ_t_trxn_card_payments_merchant_reference UNIQUE (merchant_reference)
    );
END;
GO
