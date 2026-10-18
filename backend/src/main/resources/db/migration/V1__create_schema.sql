CREATE TABLE clients (
    id UUID PRIMARY KEY,
    name VARCHAR(140) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    risk_profile VARCHAR(40) NOT NULL,
    industry VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE investments (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL REFERENCES clients(id),
    name VARCHAR(160) NOT NULL,
    asset_type VARCHAR(80) NOT NULL,
    sector VARCHAR(100) NOT NULL,
    invested_amount NUMERIC(19,2) NOT NULL,
    current_value NUMERIC(19,2) NOT NULL,
    realized_gain NUMERIC(19,2) NOT NULL,
    purchase_date DATE NOT NULL
);
CREATE INDEX idx_investments_client ON investments(client_id);

CREATE TABLE revenues (
    id UUID PRIMARY KEY,
    client_id UUID REFERENCES clients(id),
    description VARCHAR(180) NOT NULL,
    category VARCHAR(80) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    transaction_date DATE NOT NULL
);
CREATE INDEX idx_revenues_date ON revenues(transaction_date);

CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    client_id UUID REFERENCES clients(id),
    description VARCHAR(180) NOT NULL,
    category VARCHAR(80) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    transaction_date DATE NOT NULL
);
CREATE INDEX idx_expenses_date ON expenses(transaction_date);

CREATE TABLE valuations (
    id UUID PRIMARY KEY,
    investment_id UUID NOT NULL REFERENCES investments(id) ON DELETE CASCADE,
    valuation_month DATE NOT NULL,
    valuation_value NUMERIC(19,2) NOT NULL,
    CONSTRAINT uq_valuation_investment_month UNIQUE(investment_id, valuation_month)
);
CREATE INDEX idx_valuations_month ON valuations(valuation_month);
