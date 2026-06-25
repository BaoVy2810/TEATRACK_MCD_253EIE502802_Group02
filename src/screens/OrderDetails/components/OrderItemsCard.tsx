import React from 'react';
import { View, Text, Image, StyleSheet } from 'react-native';
import { OrderItem } from '../types';
import { formatPrice, formatCustomizations } from '../utils';

interface Props {
  items: OrderItem[];
}

// ── Single item row ───────────────────────────────────────────────────────────

interface ItemRowProps {
  item: OrderItem;
  isLast: boolean;
}

const ItemRow: React.FC<ItemRowProps> = ({ item, isLast }) => {
  const customStr = formatCustomizations(item);
  const lineTotal = item.unitPrice * item.quantity;
  const hasImage = !!item.image;

  return (
    <>
      <View style={styles.row}>
        {/* Thumbnail */}
        <View style={styles.imgWrapper}>
          {hasImage ? (
            <Image
              source={{ uri: item.image }}
              style={styles.img}
              resizeMode="cover"
            />
          ) : (
            <View style={[styles.img, styles.imgPlaceholder]}>
              <Text style={styles.imgPlaceholderLetter}>
                {item.productName[0]?.toUpperCase() ?? '?'}
              </Text>
            </View>
          )}
        </View>

        {/* Name + customizations */}
        <View style={styles.info}>
          <Text style={styles.productName} numberOfLines={2}>
            {item.productName}
          </Text>
          <Text style={styles.customizations}>{customStr}</Text>
          {item.toppings && item.toppings.length > 0 && (
            <Text style={styles.toppings}>+ {item.toppings.join(', ')}</Text>
          )}
        </View>

        {/* Qty + price */}
        <View style={styles.priceBlock}>
          <Text style={styles.lineTotal}>{formatPrice(lineTotal)}</Text>
          <Text style={styles.unitQty}>
            {item.quantity} × {formatPrice(item.unitPrice)}
          </Text>
        </View>
      </View>

      {!isLast && <View style={styles.itemSep} />}
    </>
  );
};

// ── Card ──────────────────────────────────────────────────────────────────────

const OrderItemsCard: React.FC<Props> = ({ items }) => (
  <View style={styles.card}>
    <Text style={styles.sectionTitle}>ORDER ITEMS</Text>
    {items.map((item, idx) => (
      <ItemRow key={item.id} item={item} isLast={idx === items.length - 1} />
    ))}
  </View>
);

export default OrderItemsCard;

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
  row: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    paddingVertical: 12,
  },
  imgWrapper: {
    borderRadius: 12,
    overflow: 'hidden',
    marginRight: 12,
  },
  img: {
    width: 56,
    height: 56,
    borderRadius: 12,
  },
  imgPlaceholder: {
    backgroundColor: '#EFF6FF',
    justifyContent: 'center',
    alignItems: 'center',
  },
  imgPlaceholderLetter: {
    fontSize: 20,
    fontWeight: '700',
    color: '#1E4ED8',
  },
  info: {
    flex: 1,
    marginRight: 8,
  },
  productName: {
    fontSize: 14,
    fontWeight: '600',
    color: '#111827',
    lineHeight: 19,
  },
  customizations: {
    fontSize: 12,
    color: '#6B7280',
    marginTop: 3,
    lineHeight: 17,
  },
  toppings: {
    fontSize: 11,
    color: '#9CA3AF',
    marginTop: 2,
  },
  priceBlock: {
    alignItems: 'flex-end',
    justifyContent: 'flex-start',
    minWidth: 80,
  },
  lineTotal: {
    fontSize: 14,
    fontWeight: '700',
    color: '#111827',
  },
  unitQty: {
    fontSize: 11,
    color: '#9CA3AF',
    marginTop: 3,
  },
  itemSep: {
    height: 1,
    backgroundColor: '#F9FAFB',
    marginLeft: 68,
  },
});
