import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

export interface DashboardSummary {
  clientCount?: number;
  investmentCount?: number;
  accountingProfit?: number;
  investedAmount?: number;
  currentPortfolioValue?: number;
  realizedReturn?: number;
  unrealizedReturn?: number;
  economicReturn?: number;
  totalRoi?: number;
  clientsWithHoldings?: number;
  profitableClients?: number;
  lossMakingClients?: number;
  totalRevenue?: number;
  totalExpenses?: number;
  total_clients?: number;
  total_investments?: number;
  total_revenue?: number;
  total_expenses?: number;
  net_profit?: number;
  total_capital_invested?: number;
  current_portfolio_value?: number;
  total_roi?: number;
  clients_with_holdings?: number;
  profitable_clients?: number;
  loss_making_clients?: number;
  currency?: string;
  [key: string]: unknown;
}

export interface AssetAllocation {
  [key: string]: unknown;
  assetType: string;
  investmentCount: number;
  investedAmount: number;
  currentValue: number;
  realizedReturn: number;
  unrealizedReturn: number;
  economicReturn: number;
}

export interface TrendPoint {
  accountingProfit?: number;
  portfolioValue?: number;
  period?: string;
  date?: string;
  month?: string;
  revenue?: number;
  expenses?: number;
  profit?: number;
  [key: string]: unknown;
}

export interface Client {
  id?: number | string;
  name?: string;
  email?: string;
  company?: string;
  phone?: string;
  status?: string;
  industry?: string;
  riskProfile?: string;
  [key: string]: unknown;
}

export interface InvestmentRequest {
  clientId: string;
  name: string;
  assetType: string;
  sector: string;
  investedAmount: number;
  currentValue: number;
  realizedGain: number;
  purchaseDate: string;
}

export interface Investment extends InvestmentRequest {
  id: string;
  [key: string]: unknown;
}

export interface LedgerRequest {
  description: string;
  category: string;
  amount: number;
  date: string;
  clientId?: string;
}

export interface LedgerEntry extends LedgerRequest {
  id?: string;
  [key: string]: unknown;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  role: string;
}

export interface PageResult<T> {
  items: T[];
  total: number;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1';
  private readonly tokenKey = 'intellibank-access-token';

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, { email, password });
  }

  setAccessToken(token: string): void {
    sessionStorage.setItem(this.tokenKey, token);
  }

  setRole(role: string): void {
    sessionStorage.setItem('intellibank-role', role);
  }

  clearAccessToken(): void {
    sessionStorage.removeItem(this.tokenKey);
    sessionStorage.removeItem('intellibank-role');
  }

  hasAccessToken(): boolean {
    return Boolean(sessionStorage.getItem(this.tokenKey));
  }

  getRole(): string {
    return sessionStorage.getItem('intellibank-role') ?? '';
  }

  getSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.baseUrl}/dashboard/summary`).pipe(
      map(value => ({
        ...value,
        total_clients: value.total_clients ?? value.clientCount,
        total_investments: value.total_investments ?? value.investmentCount,
        total_revenue: value.total_revenue ?? value.totalRevenue,
        total_expenses: value.total_expenses ?? value.totalExpenses,
        net_profit: value.net_profit ?? value.accountingProfit,
        total_capital_invested: value.total_capital_invested ?? value.investedAmount,
        current_portfolio_value: value.current_portfolio_value ?? value.currentPortfolioValue,
        total_roi: value.total_roi ?? value.totalRoi,
        clients_with_holdings: value.clients_with_holdings ?? value.clientsWithHoldings,
        profitable_clients: value.profitable_clients ?? value.profitableClients,
        loss_making_clients: value.loss_making_clients ?? value.lossMakingClients
      }))
    );
  }

  getTrends(): Observable<TrendPoint[] | { items?: TrendPoint[]; data?: TrendPoint[] }> {
    return this.http.get<TrendPoint[] | { items?: TrendPoint[]; data?: TrendPoint[] }>(
      `${this.baseUrl}/dashboard/trends`
    ).pipe(map(value => {
      const points = Array.isArray(value) ? value : value.items ?? value.data ?? [];
      const normalized = points.map(point => ({
        ...point,
        period: point.period ?? point.month,
        profit: point.profit ?? point.accountingProfit
      }));
      return Array.isArray(value) ? normalized : { ...value, items: normalized };
    })
    );
  }

  getClients(search: string, page: number, pageSize: number): Observable<Client[] | { items?: Client[]; data?: Client[]; total?: number }> {
    void search;
    void page;
    void pageSize;
    return this.http.get<Client[] | { items?: Client[]; data?: Client[]; total?: number }>(
      `${this.baseUrl}/clients`
    );
  }

  createClient(client: Partial<Client>): Observable<Client> {
    return this.http.post<Client>(`${this.baseUrl}/clients`, client);
  }

  updateClient(id: number | string, client: Partial<Client>): Observable<Client> {
    return this.http.put<Client>(`${this.baseUrl}/clients/${encodeURIComponent(id)}`, client);
  }

  deleteClient(id: number | string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/clients/${encodeURIComponent(id)}`);
  }

  getInvestments(): Observable<unknown[] | { items?: unknown[]; data?: unknown[] }> {
    return this.http.get<unknown[] | { items?: unknown[]; data?: unknown[] }>(`${this.baseUrl}/investments`);
  }

  createInvestment(investment: InvestmentRequest): Observable<Investment> {
    return this.http.post<Investment>(`${this.baseUrl}/investments`, investment);
  }

  updateInvestment(id: string | number, investment: InvestmentRequest): Observable<Investment> {
    return this.http.put<Investment>(`${this.baseUrl}/investments/${encodeURIComponent(String(id))}`, investment);
  }

  deleteInvestment(id: string | number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/investments/${encodeURIComponent(String(id))}`);
  }

  getLedgerEntries(): Observable<unknown[] | { items?: unknown[]; data?: unknown[] }> {
    return this.http.get<unknown[] | { items?: unknown[]; data?: unknown[] }>(`${this.baseUrl}/ledger`);
  }

  createLedgerEntry(entry: LedgerRequest): Observable<LedgerEntry> {
    return this.http.post<LedgerEntry>(`${this.baseUrl}/ledger`, entry);
  }

  getBankPnl(): Observable<unknown[] | Record<string, unknown>> {
    return this.http.get<unknown[] | Record<string, unknown>>(`${this.baseUrl}/reports/bank/pnl`);
  }

  getTopClients(): Observable<unknown[] | { items?: unknown[]; data?: unknown[] }> {
    return this.http.get<unknown[] | { items?: unknown[]; data?: unknown[] }>(`${this.baseUrl}/analytics/top-clients`);
  }

  getBottomClients(): Observable<unknown[] | { items?: unknown[]; data?: unknown[] }> {
    return this.http.get<unknown[] | { items?: unknown[]; data?: unknown[] }>(`${this.baseUrl}/analytics/bottom-clients`);
  }

  getPortfolio(): Observable<unknown[] | { items?: unknown[]; data?: unknown[] }> {
    return this.http.get<unknown[] | { items?: unknown[]; data?: unknown[] }>(`${this.baseUrl}/analytics/portfolio`);
  }

  getAssetAllocation(): Observable<AssetAllocation[]> {
    return this.http.get<AssetAllocation[]>(`${this.baseUrl}/analytics/allocation`);
  }

  getSectors(): Observable<unknown[] | { items?: unknown[]; data?: unknown[] }> {
    return this.http.get<unknown[] | { items?: unknown[]; data?: unknown[] }>(`${this.baseUrl}/analytics/sectors`);
  }

  askChat(message: string): Observable<{ response?: string; answer?: string; message?: string; [key: string]: unknown }> {
    return this.http.post<{ response?: string; answer?: string; message?: string; [key: string]: unknown }>(
      '/api/assistant/chat', { message }
    );
  }
}
