import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnDestroy, ViewChild, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import {
  ApiService,
  AssetAllocation,
  Client,
  DashboardSummary,
  InvestmentRequest,
  LedgerRequest,
  TrendPoint
} from './api.service';

Chart.register(...registerables);

type ViewName = 'Overview' | 'Clients' | 'Investments' | 'P&L' | 'Analytics' | 'Revenue & expenses' | 'AI assistant';
interface ChatMessage { role: 'assistant' | 'user'; text: string; }
interface InvestmentForm {
  clientId: string;
  name: string;
  assetType: string;
  sector: string;
  investedAmount: string;
  currentValue: string;
  realizedGain: string;
  purchaseDate: string;
}
interface LedgerForm {
  description: string;
  category: string;
  amount: string;
  date: string;
  clientId: string;
}

@Component({
  selector: 'ib-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnDestroy {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);
  registeredCustomers: any[] = [];
  customersError = '';
  customersLoading = true;
  @ViewChild('trendCanvas') trendCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('portfolioCanvas') portfolioCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('overviewAllocationCanvas') overviewAllocationCanvas?: ElementRef<HTMLCanvasElement>;

  readonly navItems: { label: ViewName; icon: string }[] = [
    { label: 'Overview', icon: '◫' },
    { label: 'Clients', icon: '♙' },
    { label: 'Investments', icon: '↗' },
    { label: 'P&L', icon: '▥' },
    { label: 'Analytics', icon: '◔' },
    { label: 'Revenue & expenses', icon: '⇄' },
    { label: 'AI assistant', icon: '✧' }
  ];
  readonly suggestions = [
    'Show the top 5 profitable clients',
    'Show the bottom 5 clients by profit',
    'Summarize the latest portfolio performance'
  ];
  activeView: ViewName = 'Overview';
  authenticated = this.api.hasAccessToken();
  loginEmail = 'admin@intellibank.local';
  loginPassword = '';
  loginLoading = false;
  loginError = '';
  userRole = this.api.getRole();
  mobileNavOpen = false;
  loading = false;
  error = '';
  summary: DashboardSummary | null = null;
  trends: TrendPoint[] = [];
  clients: Client[] = [];
  private allClients: Client[] = [];
  totalClients = 0;
  page = 1;
  pageSize = 10;
  search = '';
  clientLoading = false;
  clientError = '';
  clientModal = false;
  editingClient: Client | null = null;
  clientForm: Partial<Client> = {};
  savingClient = false;
  deletePending: Client | null = null;
  investments: Record<string, unknown>[] = [];
  investmentLoading = false;
  investmentError = '';
  investmentModal = false;
  investmentSaving = false;
  investmentFormError = '';
  editingInvestment: Record<string, unknown> | null = null;
  investmentDeletePending: Record<string, unknown> | null = null;
  investmentClients: Client[] = [];
  investmentClientsLoading = false;
  investmentClientsError = '';
  investmentForm: InvestmentForm = this.emptyInvestmentForm();
  ledgerEntries: Record<string, unknown>[] = [];
  ledgerLoading = false;
  ledgerError = '';
  ledgerSaving = false;
  ledgerFormError = '';
  ledgerForm: LedgerForm = this.emptyLedgerForm();
  ledgerFilter: 'ALL' | 'REVENUE' | 'EXPENSE' = 'ALL';
  pnl: Record<string, unknown>[] = [];
  topClients: Record<string, unknown>[] = [];
  bottomClients: Record<string, unknown>[] = [];
  assetAllocations: AssetAllocation[] = [];
  sectors: Record<string, unknown>[] = [];
  dataLoading = false;
  dataError = '';
  chatInput = '';
  chatLoading = false;
  chatError = '';
  chatMessages: ChatMessage[] = [
    { role: 'assistant', text: 'Hello. I can help you explore the financial data available in your IntelliBank workspace.' }
  ];
  private trendChart?: Chart;
  private portfolioChart?: Chart;
  private clientSearchTimer?: ReturnType<typeof setTimeout>;

  constructor() {
    if (this.authenticated) { this.loadOverview(); this.loadRegisteredCustomers(); this.loadAnalytics(); }
  }

  loadRegisteredCustomers(): void {
    this.customersLoading = true; this.customersError = '';
    this.http.get<any[]>('/api/v1/admin/customers').subscribe({next: rows => {this.registeredCustomers=rows;this.customersLoading=false;},error: () => {this.customersError='Could not load registered customers.';this.customersLoading=false;}});
  }

  ngOnDestroy(): void {
    this.trendChart?.destroy();
    this.portfolioChart?.destroy();
    if (this.clientSearchTimer) clearTimeout(this.clientSearchTimer);
  }

  navigate(view: ViewName): void {
    this.activeView = view;
    this.mobileNavOpen = false;
    this.error = '';
    this.dataError = '';
    if (view === 'Overview') this.loadOverview();
    if (view === 'Overview') this.loadAnalytics();
    if (view === 'Clients') this.loadClients();
    if (view === 'Investments') this.loadInvestments();
    if (view === 'P&L') this.loadPnl();
    if (view === 'Analytics') this.loadAnalytics();
    if (view === 'Revenue & expenses') this.loadLedger();
  }

  get currencyCode(): string | undefined {
    const code = this.summary?.['currency'];
    return typeof code === 'string' && code.trim() ? code : undefined;
  }

  get clientPages(): number {
    return Math.max(1, Math.ceil(this.totalClients / this.pageSize));
  }

  login(): void {
    this.loginLoading = true;
    this.loginError = '';
    this.api.login(this.loginEmail, this.loginPassword).subscribe({
      next: result => {
        this.api.setAccessToken(result.accessToken);
        this.api.setRole(result.role);
        this.userRole = result.role;
        this.authenticated = true;
        this.loginPassword = '';
        this.loginLoading = false;
        this.loadOverview();
        this.loadRegisteredCustomers();
        this.loadAnalytics();
      },
      error: err => {
        this.loginError = this.errorMessage(err);
        this.loginLoading = false;
      }
    });
  }

  logout(): void {
    this.api.clearAccessToken();
    this.router.navigateByUrl('/');
    this.authenticated = false;
    this.userRole = '';
    this.summary = null;
    this.trends = [];
    this.clients = [];
    this.allClients = [];
    this.investments = [];
    this.pnl = [];
    this.topClients = [];
    this.bottomClients = [];
    this.assetAllocations = [];
    this.sectors = [];
    this.chatMessages = [{ role: 'assistant', text: 'Hello. I can help you explore the financial data available in your IntelliBank workspace.' }];
    this.trendChart?.destroy();
    this.portfolioChart?.destroy();
  }

  get displayedTrend(): TrendPoint[] { return this.trends; }
  get Math(): Math { return Math; }

  get pageDescription(): string {
    const descriptions: Record<ViewName, string> = {
      Overview: 'Here’s what’s happening across your banking portfolio.',
      Clients: 'Manage the relationships at the heart of your portfolio.',
      Investments: 'Create and manage investment positions in your portfolio.',
      'P&L': 'Understand bank-level revenue, expenses, and profitability.',
      Analytics: 'Explore client contribution and portfolio allocation.',
      'Revenue & expenses': 'Record and review revenue and expense ledger entries.',
      'AI assistant': 'Ask questions and get context from connected banking data.'
    };
    return descriptions[this.activeView];
  }

  objectKeys(value: Record<string, unknown> | null | undefined): string[] {
    return value ? Object.keys(value) : [];
  }

  loadOverview(): void {
    this.loading = true;
    this.error = '';
    this.api.getSummary().subscribe({
      next: value => { this.summary = value; this.loading = false; },
      error: err => { this.error = this.errorMessage(err); this.loading = false; }
    });
    this.api.getTrends().subscribe({
      next: value => {
        this.trends = this.asArray<TrendPoint>(value);
        setTimeout(() => this.renderTrendChart(), 0);
      },
      error: err => { if (!this.error) this.error = this.errorMessage(err); }
    });
  }

  loadClients(): void {
    this.clientLoading = true;
    this.clientError = '';
    this.api.getClients(this.search, this.page, this.pageSize).subscribe({
      next: value => {
        this.allClients = this.asArray<Client>(value);
        const query = this.search.trim().toLocaleLowerCase();
        const filtered = query
          ? this.allClients.filter(client =>
              [client.name, client.email, client.industry]
                .some(field => field?.toLocaleLowerCase().includes(query)))
          : this.allClients;
        this.totalClients = filtered.length;
        this.clients = filtered.slice((this.page - 1) * this.pageSize, this.page * this.pageSize);
        this.clientLoading = false;
      },
      error: err => { this.clientError = this.errorMessage(err); this.clientLoading = false; }
    });
  }

  onSearchChange(): void {
    this.page = 1;
    if (this.clientSearchTimer) clearTimeout(this.clientSearchTimer);
    this.clientSearchTimer = setTimeout(() => this.loadClients(), 300);
  }

  changePage(delta: number): void {
    const next = this.page + delta;
    if (next >= 1 && next <= this.clientPages) { this.page = next; this.loadClients(); }
  }

  openClientForm(client?: Client): void {
    this.editingClient = client ?? null;
    this.clientForm = client ? { ...client } : {};
    this.clientModal = true;
  }

  saveClient(): void {
    this.savingClient = true;
    const request = this.editingClient?.id !== undefined
      ? this.api.updateClient(this.editingClient.id, this.clientForm)
      : this.api.createClient(this.clientForm);
    request.subscribe({
      next: () => { this.clientModal = false; this.savingClient = false; this.loadClients(); },
      error: err => { this.clientError = this.errorMessage(err); this.savingClient = false; }
    });
  }

  confirmDelete(client: Client): void { this.deletePending = client; }

  deleteClient(): void {
    if (this.deletePending?.id === undefined) return;
    this.api.deleteClient(this.deletePending.id).subscribe({
      next: () => { this.deletePending = null; this.loadClients(); },
      error: err => { this.clientError = this.errorMessage(err); this.deletePending = null; }
    });
  }

  loadInvestments(): void {
    this.investmentLoading = true;
    this.investmentError = '';
    this.api.getInvestments().subscribe({
      next: result => { this.investments = this.records(result); this.investmentLoading = false; },
      error: err => { this.investmentError = this.errorMessage(err); this.investmentLoading = false; }
    });
  }

  openInvestmentForm(investment?: Record<string, unknown>): void {
    this.editingInvestment = investment ?? null;
    this.investmentFormError = '';
    this.investmentForm = investment ? {
      clientId: String(investment['clientId'] ?? ''),
      name: String(investment['name'] ?? ''),
      assetType: String(investment['assetType'] ?? ''),
      sector: String(investment['sector'] ?? ''),
      investedAmount: this.inputNumber(investment['investedAmount']),
      currentValue: this.inputNumber(investment['currentValue']),
      realizedGain: this.inputNumber(investment['realizedGain']),
      purchaseDate: this.inputDate(investment['purchaseDate'])
    } : this.emptyInvestmentForm();
    this.investmentModal = true;
    this.loadInvestmentClients();
  }

  saveInvestment(): void {
    const form = this.investmentForm;
    const payload: InvestmentRequest = {
      clientId: form.clientId,
      name: form.name.trim(),
      assetType: form.assetType.trim(),
      sector: form.sector.trim(),
      investedAmount: Number(form.investedAmount),
      currentValue: Number(form.currentValue),
      realizedGain: Number(form.realizedGain),
      purchaseDate: form.purchaseDate
    };
    this.investmentSaving = true;
    this.investmentFormError = '';
    const id = this.editingInvestment?.['id'];
    const request = typeof id === 'string' || typeof id === 'number'
      ? this.api.updateInvestment(id, payload)
      : this.api.createInvestment(payload);
    request.subscribe({
      next: () => {
        this.investmentModal = false;
        this.investmentSaving = false;
        this.loadInvestments();
        this.loadOverview();
      },
      error: err => { this.investmentFormError = this.errorMessage(err); this.investmentSaving = false; }
    });
  }

  deleteInvestment(): void {
    const id = this.investmentDeletePending?.['id'];
    if (typeof id !== 'string' && typeof id !== 'number') return;
    this.investmentLoading = true;
    this.investmentError = '';
    this.api.deleteInvestment(id).subscribe({
      next: () => {
        this.investmentDeletePending = null;
        this.loadInvestments();
        this.loadOverview();
      },
      error: err => {
        this.investmentError = this.errorMessage(err);
        this.investmentDeletePending = null;
        this.investmentLoading = false;
      }
    });
  }

  private loadInvestmentClients(): void {
    this.investmentClientsLoading = true;
    this.investmentClientsError = '';
    this.api.getClients('', 1, 1000).subscribe({
      next: result => {
        this.investmentClients = this.asArray<Client>(result);
        this.investmentClientsLoading = false;
      },
      error: err => {
        this.investmentClientsError = this.errorMessage(err);
        this.investmentClientsLoading = false;
      }
    });
  }

  loadLedger(): void {
    this.ledgerLoading = true;
    this.ledgerError = '';
    this.loadInvestmentClients();
    this.api.getLedgerEntries().subscribe({
      next: result => { this.ledgerEntries = this.records(result); this.ledgerLoading = false; },
      error: err => { this.ledgerError = this.errorMessage(err); this.ledgerLoading = false; }
    });
  }

  get filteredLedgerEntries(): Record<string, unknown>[] {
    if (this.ledgerFilter === 'ALL') return this.ledgerEntries;
    return this.ledgerEntries.filter(entry => String(entry['category'] ?? '').toUpperCase() === this.ledgerFilter);
  }

  recordLedgerEntry(): void {
    const form = this.ledgerForm;
    const payload: LedgerRequest = {
      description: form.description.trim(),
      category: form.category,
      amount: Number(form.amount),
      date: form.date
    };
    if (form.clientId) payload.clientId = form.clientId;
    this.ledgerSaving = true;
    this.ledgerFormError = '';
    this.api.createLedgerEntry(payload).subscribe({
      next: () => {
        this.ledgerForm = this.emptyLedgerForm();
        this.ledgerSaving = false;
        this.loadLedger();
        this.loadOverview();
      },
      error: err => { this.ledgerFormError = this.errorMessage(err); this.ledgerSaving = false; }
    });
  }

  clientName(clientId: unknown): string {
    if (clientId === null || clientId === undefined || clientId === '') return '—';
    const client = this.investmentClients.find(item => String(item.id) === String(clientId));
    return client?.name ?? String(clientId);
  }

  categoryLabel(value: unknown): string {
    const category = String(value ?? '').toUpperCase();
    if (category === 'REVENUE') return 'Revenue';
    if (category === 'EXPENSE') return 'Expense';
    return this.displayValue(value);
  }

  isExpenseCategory(value: unknown): boolean {
    return String(value ?? '').toUpperCase() === 'EXPENSE';
  }

  private emptyInvestmentForm(): InvestmentForm {
    return { clientId: '', name: '', assetType: '', sector: '', investedAmount: '', currentValue: '', realizedGain: '', purchaseDate: '' };
  }

  private emptyLedgerForm(): LedgerForm {
    return { description: '', category: 'REVENUE', amount: '', date: '', clientId: '' };
  }

  private inputNumber(value: unknown): string {
    return typeof value === 'number' && Number.isFinite(value) ? String(value) : '';
  }

  private inputDate(value: unknown): string {
    return typeof value === 'string' ? value.slice(0, 10) : '';
  }

  loadPnl(): void {
    this.dataLoading = true;
    this.api.getBankPnl().subscribe({
      next: result => { this.pnl = this.records(result); this.dataLoading = false; },
      error: err => { this.dataError = this.errorMessage(err); this.dataLoading = false; }
    });
  }

  loadAnalytics(): void {
    this.dataLoading = true;
    this.dataError = '';
    let completed = 0;
    const finish = (): void => { completed += 1; if (completed === 4) this.dataLoading = false; };
    this.api.getTopClients().subscribe({
      next: result => { this.topClients = this.records(result); finish(); },
      error: err => { this.dataError = this.errorMessage(err); finish(); }
    });
    this.api.getBottomClients().subscribe({
      next: result => { this.bottomClients = this.records(result); finish(); },
      error: err => { this.dataError = this.errorMessage(err); finish(); }
    });
    this.api.getAssetAllocation().subscribe({
      next: result => {
        this.assetAllocations = result;
        finish();
        setTimeout(() => this.renderPortfolioChart(), 0);
      },
      error: err => { this.dataError = this.errorMessage(err); finish(); }
    });
    this.api.getSectors().subscribe({
      next: result => {
        this.sectors = this.records(result);
        finish();
      },
      error: err => { this.dataError = this.errorMessage(err); finish(); }
    });
  }

  sendChat(prompt?: string): void {
    const message = (prompt ?? this.chatInput).trim();
    if (!message || this.chatLoading) return;
    this.chatMessages.push({ role: 'user', text: message });
    this.chatInput = '';
    this.chatLoading = true;
    this.chatError = '';
    this.api.askChat(message).subscribe({
      next: result => {
        this.chatMessages.push({
          role: 'assistant',
          text: result.response ?? result.answer ?? result.message ?? 'The assistant returned no text response.'
        });
        this.chatLoading = false;
      },
      error: err => { this.chatError = this.errorMessage(err); this.chatLoading = false; }
    });
  }

  exportCsv(rows: Record<string, unknown>[], filename: string): void {
    if (!rows.length) return;
    const columns = [...new Set(rows.flatMap(row => Object.keys(row)))];
    const escape = (value: unknown): string => `"${String(value ?? '').replaceAll('"', '""')}"`;
    const content = [columns.map(escape).join(','), ...rows.map(row => columns.map(key => escape(row[key])).join(','))].join('\r\n');
    const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = filename;
    link.click();
    URL.revokeObjectURL(link.href);
  }

  exportClients(): void { this.exportCsv(this.clients as Record<string, unknown>[], 'clients.csv'); }

  valueFor(row: Record<string, unknown>, keys: string[]): unknown {
    for (const key of keys) if (row[key] !== undefined && row[key] !== null) return row[key];
    return undefined;
  }

  displayValue(value: unknown): string {
    if (value === null || value === undefined || value === '') return '—';
    if (typeof value === 'object') return JSON.stringify(value);
    return String(value);
  }

  isAmountKey(key: string): boolean {
    return /amount|value|balance|revenue|expense|profit|return|aum|capital|cost|fee|yield|investment/i.test(key);
  }

  formatAmount(value: unknown): string {
    if (typeof value !== 'number' || !Number.isFinite(value)) return this.displayValue(value);
    if (this.currencyCode) {
      try {
        return new Intl.NumberFormat(undefined, { style: 'currency', currency: this.currencyCode, maximumFractionDigits: 2 }).format(value);
      } catch {
        return `${this.currencyCode} ${value.toLocaleString()}`;
      }
    }
    return new Intl.NumberFormat(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value);
  }

  displayKey(key: string): string {
    return key.replaceAll('_', ' ').replace(/\b\w/g, letter => letter.toUpperCase());
  }

  trackByIndex(index: number): number { return index; }

  private renderTrendChart(): void {
    const canvas = this.trendCanvas?.nativeElement;
    if (!canvas || !this.trends.length) return;
    this.trendChart?.destroy();
    const labels = this.trends.map(point => point.period ?? point.month ?? point.date ?? '');
    const datasets: ChartConfiguration<'line'>['data']['datasets'] = [];
    const series = [
      { key: 'revenue', label: 'Revenue', color: '#177e89' },
      { key: 'expenses', label: 'Expenses', color: '#ee9b48' },
      { key: 'profit', label: 'Profit', color: '#4567a8' }
    ] as const;
    for (const item of series) {
      if (this.trends.some(point => typeof point[item.key] === 'number')) {
        datasets.push({
          label: item.label, data: this.trends.map(point => typeof point[item.key] === 'number' ? point[item.key] as number : null),
          borderColor: item.color, backgroundColor: item.color, tension: 0.35, pointRadius: 2, spanGaps: true
        });
      }
    }
    this.trendChart = new Chart(canvas, {
      type: 'line', data: { labels, datasets },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom', labels: { usePointStyle: true, boxWidth: 7, padding: 18 } } },
        scales: { y: { grid: { color: '#eef2f5' }, ticks: { callback: value => this.formatAmount(Number(value)) } }, x: { grid: { display: false } } }
      }
    });
  }

  private renderPortfolioChart(): void {
    const canvas = (this.activeView === 'Overview' ? this.overviewAllocationCanvas : this.portfolioCanvas)?.nativeElement;
    if (!canvas || !this.assetAllocations.length) return;
    this.portfolioChart?.destroy();
    this.portfolioChart = new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels: this.assetAllocations.map(item => item.assetType),
        datasets: [{ data: this.assetAllocations.map(item => item.currentValue), backgroundColor: ['#177e89', '#4567a8', '#ee9b48', '#8e7db5', '#55a889', '#d46b63'], borderWidth: 0 }]
      },
      options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { usePointStyle: true, boxWidth: 7 } } } }
    });
  }

  private asArray<T>(value: unknown): T[] {
    if (Array.isArray(value)) return value as T[];
    if (value && typeof value === 'object') {
      const record = value as {
        items?: unknown;
        data?: unknown;
        results?: unknown;
        clients?: unknown;
        trends?: unknown;
        investments?: unknown;
        ledgerEntries?: unknown;
        entries?: unknown;
      };
      const candidate = record.items ?? record.data ?? record.results ?? record.clients ?? record.trends
        ?? record.investments ?? record.ledgerEntries ?? record.entries;
      return Array.isArray(candidate) ? candidate as T[] : [];
    }
    return [];
  }

  private asPage<T>(value: unknown): { items: T[]; total: number } {
    const items = this.asArray<T>(value);
    const total = value && !Array.isArray(value) && typeof value === 'object'
      ? Number((value as { total?: number; total_count?: number }).total ?? (value as { total_count?: number }).total_count ?? items.length)
      : items.length;
    return { items, total: Number.isFinite(total) ? total : items.length };
  }

  private records(value: unknown): Record<string, unknown>[] {
    const rows = this.asArray<unknown>(value);
    if (rows.length) return rows.filter(item => item !== null && typeof item === 'object') as Record<string, unknown>[];
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      const envelope = value as {
        items?: unknown;
        data?: unknown;
        results?: unknown;
        clients?: unknown;
        trends?: unknown;
        investments?: unknown;
        ledgerEntries?: unknown;
        entries?: unknown;
      };
      if (Array.isArray(envelope.items) || Array.isArray(envelope.data) || Array.isArray(envelope.results)
        || Array.isArray(envelope.clients) || Array.isArray(envelope.trends) || Array.isArray(envelope.investments)
        || Array.isArray(envelope.ledgerEntries) || Array.isArray(envelope.entries)) return [];
    }
    return value && typeof value === 'object' && !Array.isArray(value)
      ? [value as Record<string, unknown>]
      : [];
  }

  private errorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const detail = error.error?.detail ?? error.error?.message;
      return typeof detail === 'string' ? detail : `${error.message || 'Request failed'} (HTTP ${error.status || 'network error'}).`;
    }
    return 'Unable to load data from the banking service.';
  }
}
