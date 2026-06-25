import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Order } from '../types';
import { formatPrice } from '../utils';

interface Props {
  order: Pick<Order, 'subtotal' | 'discount' | 'shippingFee' | 'total'>;
}

// ── Row ───────────────────────────────────────────────────────────────────────

interface RowProps {
  label: string;
  value: string;
  labelStyle?: object;
  valueStyle?: object;
}

const SummaryRow: React.FC<RowProps> = ({ label, value, labelStyle, valueStyle }) => (
  <View style={rowStyles.row}>
    <Text style={[rowStyles.label, labelStyle]}>{label}</Text>
    <Text style={[rowStyles.value, valueStyle]}>{value}</Text>
  </View>
);

const rowStyles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 7,
  },
  label: {
    fontSize: 13,
    color: '#6B7280',
  },
  value: {
    fontSize: 13,
    fontWeight: '500',
    color: '#374151',
  },
});

// ── Card ──────────────────────────────────────────────────────────────────────

const PaymentSummaryCard: React.FC<Props> = ({ order }) => {
  const freeShipping = order.shippingFee === 0;

  return (
    <View style={styles.card}>
      <Text style={styles.sectionTitle}>PAYMENT SUMMARY</Text>

      <SummaryRow label="Subtotal" value={formatPrice(order.subtotal)} />

      {order.discount > 0 && (
        <SummaryRow
          label="Discount"
          value={`-${formatPrice(order.discount)}`}
          labelStyle={styles.discountLabel}
          valueStyle={styles.discountValue}
        />
      )}

      <SummaryRow
        label="Delivery fee"
        value={freeShipping ? 'Free' : formatPrice(order.shippingFee)}
        valueStyle={freeShipping ? styles.freeValue : undefined}
      />

      <View style={styles.totalDivider} />

      {/* Total — prominent */}
      <View style={styles.totalRow}>
        <View>
          <Text style={styles.totalLabel}>Total</Text>
          <Text style={styles.totalSub}>
            {order.discount > 0 ? `${formatPrice(order.discount)} saved` : 'inc. all taxes'}
          </Text>
        </View>
        <Text style={styles.totalValue}>{formatPrice(order.total)}</Text>
      </View>
    </View>
  );
};

export default PaymentSummaryCard;

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    padding: 16,
    shadowColor: '#1E4ED8',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.07,
    shadowRadius: 10,
    elevation: 4,
  },
  sectionTitle: {
    fontSize: 11,
    fontWeight: '700',
    color: '#9CA3AF',
    letterSpacing: 1,
    marginBottom: 4,
  },
  discountLabel: {
    color: '#10B981',
  },
  discountValue: {
    color: '#EF4444',
    fontWeight: '600',
  },
  freeValue: {
    color: '#10B981',
    fontWeight: '600',
  },
  totalDivider: {
    height: 1,
    backgroundColor: '#E5E7EB',
    marginVertical: 10,
  },
  totalRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 2,
  },
  totalLabel: {
    fontSize: 15,
    fontWeight: '700',
    color: '#111827',
  },
  totalSub: {
    fontSize: 11,
    color: '#9CA3AF',
    marginTop: 2,
  },
  totalValue: {
    fontSize: 22,
    fontWeight: '800',
    color: '#1E4ED8',
    letterSpacing: -0.5,
  },
});
