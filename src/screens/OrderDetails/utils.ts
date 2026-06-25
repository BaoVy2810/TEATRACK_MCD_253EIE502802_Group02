import { OrderItem, OrderStatus } from './types';

// ── Formatters ────────────────────────────────────────────────────────────────

export function formatPrice(amount: number): string {
  return new Intl.NumberFormat('vi-VN').format(amount) + '₫';
}

export function formatDate(iso: string): string {
  try {
    const d = new Date(iso);
    const day = d.getDate().toString().padStart(2, '0');
    const mon = d.toLocaleString('en-US', { month: 'short' });
    const yr = d.getFullYear();
    const h = d.getHours();
    const m = d.getMinutes().toString().padStart(2, '0');
    const ampm = h >= 12 ? 'PM' : 'AM';
    const h12 = (h % 12 || 12).toString().padStart(2, '0');
    return `${day} ${mon} ${yr} • ${h12}:${m} ${ampm}`;
  } catch {
    return iso;
  }
}

export function formatCustomizations(item: Pick<OrderItem, 'size' | 'ice' | 'sugar'>): string {
  return [`Size ${item.size}`, item.ice, item.sugar].filter(Boolean).join(' • ');
}

// ── Status config ─────────────────────────────────────────────────────────────

export interface StatusConfig {
  label: string;
  bgColor: string;
  textColor: string;
  dotColor: string;
}

export const STATUS_CONFIG: Record<OrderStatus, StatusConfig> = {
  pending:    { label: 'Pending',    bgColor: '#FEF3C7', textColor: '#92400E', dotColor: '#F59E0B' },
  processing: { label: 'Processing', bgColor: '#DBEAFE', textColor: '#1E40AF', dotColor: '#3B82F6' },
  ready:      { label: 'Ready',      bgColor: '#EDE9FE', textColor: '#5B21B6', dotColor: '#7C3AED' },
  shipping:   { label: 'Shipping',   bgColor: '#FED7AA', textColor: '#9A3412', dotColor: '#EA580C' },
  completed:  { label: 'Completed',  bgColor: '#D1FAE5', textColor: '#065F46', dotColor: '#10B981' },
  cancelled:  { label: 'Cancelled',  bgColor: '#F3F4F6', textColor: '#374151', dotColor: '#9CA3AF' },
};

// Payment method accent colors
export const PAYMENT_COLOR: Record<string, string> = {
  MoMo:    '#A61C56',
  VNPay:   '#EF4444',
  ZaloPay: '#2563EB',
  Cash:    '#16A34A',
};

export function getPaymentColor(method: string): string {
  return PAYMENT_COLOR[method] ?? '#6B7280';
}
