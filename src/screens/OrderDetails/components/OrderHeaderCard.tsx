import React from 'react';
import { View, Text, Image, StyleSheet } from 'react-native';
import { Order } from '../types';
import { formatPrice, formatDate, getPaymentColor } from '../utils';
import StatusBadge from './StatusBadge';

interface Props {
  order: Order;
}

const OrderHeaderCard: React.FC<Props> = ({ order }) => {
  const firstItem = order.items[0];
  const paymentColor = getPaymentColor(order.paymentMethod);
  const hasImage = !!firstItem?.image;

  return (
    <View style={styles.card}>
      {/* ── Top: thumbnail + order meta + status badge ── */}
      <View style={styles.topRow}>
        {/* Product thumbnail */}
        <View style={styles.imgWrapper}>
          {hasImage ? (
            <Image
              source={{ uri: firstItem.image }}
              style={styles.img}
              resizeMode="cover"
            />
          ) : (
            <View style={[styles.img, styles.imgPlaceholder]}>
              <Text style={styles.imgPlaceholderLetter}>
                {firstItem?.productName?.[0]?.toUpperCase() ?? '?'}
              </Text>
            </View>
          )}
        </View>

        {/* Order meta */}
        <View style={styles.meta}>
          <Text style={styles.orderId}>#{order.orderId}</Text>
          <Text style={styles.date}>{formatDate(order.createdAt)}</Text>
          <View style={[styles.paymentChip, { borderColor: paymentColor + '33' }]}>
            <View style={[styles.paymentDot, { backgroundColor: paymentColor }]} />
            <Text style={[styles.paymentLabel, { color: paymentColor }]}>
              {order.paymentMethod}
            </Text>
          </View>
        </View>

        {/* Status badge pinned top-right */}
        <StatusBadge status={order.status} size="sm" />
      </View>

      {/* ── Divider ── */}
      <View style={styles.divider} />

      {/* ── Bottom: stats + address ── */}
      <View style={styles.bottomSection}>
        {/* Items count + total */}
        <View style={styles.statsRow}>
          <View style={styles.statGroup}>
            <Text style={styles.statLabel}>Items</Text>
            <Text style={styles.statValue}>
              {order.items.length} {order.items.length === 1 ? 'item' : 'items'}
            </Text>
          </View>

          <View style={styles.statSep} />

          <View style={styles.statGroup}>
            <Text style={styles.statLabel}>Total</Text>
            <Text style={[styles.statValue, styles.statTotal]}>
              {formatPrice(order.total)}
            </Text>
          </View>
        </View>

        {/* Branch address — full-width, wrappable */}
        {!!order.branchAddress && (
          <View style={styles.addressRow}>
            <Text style={styles.addressPin}>📍</Text>
            <Text style={styles.addressText}>{order.branchAddress}</Text>
          </View>
        )}
      </View>
    </View>
  );
};

export default OrderHeaderCard;

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
  topRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
  imgWrapper: {
    borderRadius: 14,
    overflow: 'hidden',
    marginRight: 12,
  },
  img: {
    width: 72,
    height: 72,
    borderRadius: 14,
  },
  imgPlaceholder: {
    backgroundColor: '#EFF6FF',
    justifyContent: 'center',
    alignItems: 'center',
  },
  imgPlaceholderLetter: {
    fontSize: 26,
    fontWeight: '700',
    color: '#1E4ED8',
  },
  meta: {
    flex: 1,
    marginRight: 8,
  },
  orderId: {
    fontSize: 16,
    fontWeight: '700',
    color: '#111827',
    letterSpacing: 0.4,
  },
  date: {
    fontSize: 12,
    color: '#9CA3AF',
    marginTop: 3,
  },
  paymentChip: {
    flexDirection: 'row',
    alignItems: 'center',
    alignSelf: 'flex-start',
    borderWidth: 1,
    borderRadius: 8,
    paddingHorizontal: 8,
    paddingVertical: 3,
    marginTop: 6,
  },
  paymentDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    marginRight: 5,
  },
  paymentLabel: {
    fontSize: 12,
    fontWeight: '600',
  },
  divider: {
    height: 1,
    backgroundColor: '#F3F4F6',
    marginVertical: 14,
  },
  bottomSection: {},
  statsRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statGroup: {},
  statSep: {
    width: 1,
    height: 28,
    backgroundColor: '#E5E7EB',
    marginHorizontal: 16,
  },
  statLabel: {
    fontSize: 11,
    color: '#9CA3AF',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginBottom: 2,
  },
  statValue: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
  },
  statTotal: {
    color: '#1E4ED8',
    fontSize: 15,
    fontWeight: '700',
  },
  addressRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginTop: 12,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#F3F4F6',
  },
  addressPin: {
    fontSize: 13,
    marginRight: 6,
    marginTop: 1,
  },
  addressText: {
    flex: 1,
    fontSize: 13,
    color: '#6B7280',
    lineHeight: 19,
  },
});
