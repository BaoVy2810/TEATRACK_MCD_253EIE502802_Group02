import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Order } from '../types';
import { getPaymentColor } from '../utils';
import StatusBadge from './StatusBadge';

interface Props {
  order: Order;
}

// ── Row ───────────────────────────────────────────────────────────────────────

interface RowProps {
  icon: string;
  label: string;
  children: React.ReactNode;
  noBorder?: boolean;
}

const InfoRow: React.FC<RowProps> = ({ icon, label, children, noBorder }) => (
  <>
    <View style={rowStyles.row}>
      <Text style={rowStyles.icon}>{icon}</Text>
      <Text style={rowStyles.label}>{label}</Text>
      <View style={rowStyles.valueArea}>{children}</View>
    </View>
    {!noBorder && <View style={rowStyles.sep} />}
  </>
);

const rowStyles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    paddingVertical: 11,
  },
  icon: {
    fontSize: 15,
    width: 22,
    textAlign: 'center',
    marginTop: 1,
    marginRight: 10,
  },
  label: {
    width: 114,
    fontSize: 13,
    color: '#9CA3AF',
    marginTop: 1,
  },
  valueArea: {
    flex: 1,
    alignItems: 'flex-end',
  },
  sep: {
    height: 1,
    backgroundColor: '#F9FAFB',
    marginLeft: 32,
  },
});

// ── Card ──────────────────────────────────────────────────────────────────────

const DeliveryInfoCard: React.FC<Props> = ({ order }) => {
  const paymentColor = getPaymentColor(order.paymentMethod);

  return (
    <View style={styles.card}>
      <Text style={styles.sectionTitle}>DELIVERY INFORMATION</Text>

      <InfoRow icon="👤" label="Customer Name">
        <Text style={styles.value}>{order.customerName}</Text>
      </InfoRow>

      <InfoRow icon="📞" label="Phone Number">
        <Text style={styles.value}>{order.phoneNumber}</Text>
      </InfoRow>

      <InfoRow icon="📍" label="Delivery Address">
        <Text style={[styles.value, styles.valueRight]}>{order.deliveryAddress}</Text>
      </InfoRow>

      <InfoRow icon="💳" label="Payment Method">
        <View style={[styles.paymentChip, { borderColor: paymentColor + '40' }]}>
          <View style={[styles.paymentDot, { backgroundColor: paymentColor }]} />
          <Text style={[styles.paymentText, { color: paymentColor }]}>
            {order.paymentMethod}
          </Text>
        </View>
      </InfoRow>

      <InfoRow icon="📦" label="Order Status" noBorder>
        <StatusBadge status={order.status} size="sm" />
      </InfoRow>
    </View>
  );
};

export default DeliveryInfoCard;

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingTop: 16,
    paddingBottom: 4,
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
    marginBottom: 2,
  },
  value: {
    fontSize: 13,
    fontWeight: '500',
    color: '#111827',
    lineHeight: 19,
  },
  valueRight: {
    textAlign: 'right',
  },
  paymentChip: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderRadius: 8,
    paddingHorizontal: 8,
    paddingVertical: 3,
  },
  paymentDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    marginRight: 5,
  },
  paymentText: {
    fontSize: 12,
    fontWeight: '600',
  },
});
