import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { OrderStatus } from '../types';

interface Props {
  status: OrderStatus;
  onTrackOrder?: () => void;
  onReorder?: () => void;
  onOrderAgain?: () => void;
  onContactSupport?: () => void;
}

// ── Button ────────────────────────────────────────────────────────────────────

interface BtnProps {
  label: string;
  icon: string;
  variant: 'primary' | 'outline';
  onPress?: () => void;
}

const ActionButton: React.FC<BtnProps> = ({ label, icon, variant, onPress }) => (
  <TouchableOpacity
    style={[styles.btn, variant === 'primary' ? styles.btnPrimary : styles.btnOutline]}
    onPress={onPress}
    activeOpacity={0.75}
  >
    <Text style={styles.btnIcon}>{icon}</Text>
    <Text style={[styles.btnLabel, variant === 'outline' && styles.btnLabelOutline]}>
      {label}
    </Text>
  </TouchableOpacity>
);

// ── Hint text map ─────────────────────────────────────────────────────────────

const HINT: Record<OrderStatus, string> = {
  pending:    'Your order is confirmed and waiting to be processed.',
  processing: 'Our team is preparing your order right now.',
  ready:      'Your order is ready and will be picked up soon.',
  shipping:   'Your order is on its way! Track it in real-time.',
  completed:  'Hope you enjoyed it! Order again with one tap.',
  cancelled:  'This order was cancelled. Place a new order anytime.',
};

// ── Section ───────────────────────────────────────────────────────────────────

const ACTIVE_STATUSES: OrderStatus[] = ['pending', 'processing', 'ready', 'shipping'];

const OrderActionSection: React.FC<Props> = ({
  status,
  onTrackOrder,
  onReorder,
  onOrderAgain,
  onContactSupport,
}) => {
  const isActive = ACTIVE_STATUSES.includes(status);

  return (
    <View style={styles.container}>
      {/* pending / processing / ready / shipping */}
      {isActive && (
        <View style={styles.row}>
          <ActionButton
            label="Track Order"
            icon="🚚"
            variant="outline"
            onPress={onTrackOrder}
          />
          <ActionButton
            label="Contact Support"
            icon="💬"
            variant="primary"
            onPress={onContactSupport}
          />
        </View>
      )}

      {/* completed */}
      {status === 'completed' && (
        <View style={styles.row}>
          <ActionButton
            label="Reorder"
            icon="🔄"
            variant="primary"
            onPress={onReorder}
          />
          <ActionButton
            label="Contact Support"
            icon="💬"
            variant="outline"
            onPress={onContactSupport}
          />
        </View>
      )}

      {/* cancelled */}
      {status === 'cancelled' && (
        <View style={styles.row}>
          <ActionButton
            label="Order Again"
            icon="🛍"
            variant="primary"
            onPress={onOrderAgain}
          />
        </View>
      )}

      <Text style={styles.hint}>{HINT[status]}</Text>
    </View>
  );
};

export default OrderActionSection;

const styles = StyleSheet.create({
  container: {
    paddingBottom: 4,
  },
  row: {
    flexDirection: 'row',
    gap: 10,
  },
  btn: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    height: 50,
    borderRadius: 14,
  },
  btnPrimary: {
    backgroundColor: '#1E4ED8',
    shadowColor: '#1E4ED8',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 8,
    elevation: 5,
  },
  btnOutline: {
    backgroundColor: 'transparent',
    borderWidth: 1.5,
    borderColor: '#1E4ED8',
  },
  btnIcon: {
    fontSize: 16,
    marginRight: 7,
  },
  btnLabel: {
    fontSize: 14,
    fontWeight: '700',
    color: '#FFFFFF',
    letterSpacing: 0.2,
  },
  btnLabelOutline: {
    color: '#1E4ED8',
  },
  hint: {
    fontSize: 12,
    color: '#9CA3AF',
    textAlign: 'center',
    lineHeight: 17,
    marginTop: 12,
  },
});
