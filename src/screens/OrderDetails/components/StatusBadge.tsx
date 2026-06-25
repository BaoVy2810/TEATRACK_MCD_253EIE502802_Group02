import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { OrderStatus } from '../types';
import { STATUS_CONFIG } from '../utils';

interface Props {
  status: OrderStatus;
  size?: 'sm' | 'md' | 'lg';
}

const StatusBadge: React.FC<Props> = ({ status, size = 'md' }) => {
  const cfg = STATUS_CONFIG[status];

  return (
    <View
      style={[
        styles.badge,
        { backgroundColor: cfg.bgColor },
        size === 'sm' && styles.sm,
        size === 'lg' && styles.lg,
      ]}
    >
      <View style={[styles.dot, { backgroundColor: cfg.dotColor }]} />
      <Text
        style={[
          styles.label,
          { color: cfg.textColor },
          size === 'sm' && styles.labelSm,
          size === 'lg' && styles.labelLg,
        ]}
      >
        {cfg.label}
      </Text>
    </View>
  );
};

export default StatusBadge;

const styles = StyleSheet.create({
  badge: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 99,
  },
  sm: { paddingHorizontal: 8, paddingVertical: 3 },
  lg: { paddingHorizontal: 14, paddingVertical: 7 },
  dot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    marginRight: 5,
  },
  label: {
    fontSize: 12,
    fontWeight: '600',
  },
  labelSm: { fontSize: 11 },
  labelLg: { fontSize: 14 },
});
