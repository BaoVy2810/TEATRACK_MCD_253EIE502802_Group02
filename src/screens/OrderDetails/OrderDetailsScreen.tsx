import React from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  SafeAreaView,
  StatusBar,
} from 'react-native';
import { Order } from './types';
import OrderHeaderCard from './components/OrderHeaderCard';
import DeliveryInfoCard from './components/DeliveryInfoCard';
import OrderItemsCard from './components/OrderItemsCard';
import PaymentSummaryCard from './components/PaymentSummaryCard';
import OrderActionSection from './components/OrderActionSection';

interface Props {
  order: Order;
  onBack?: () => void;
  onTrackOrder?: () => void;
  onReorder?: () => void;
  onOrderAgain?: () => void;
  onContactSupport?: () => void;
  onSupport?: () => void;
}

const OrderDetailsScreen: React.FC<Props> = ({
  order,
  onBack,
  onTrackOrder,
  onReorder,
  onOrderAgain,
  onContactSupport,
  onSupport,
}) => {
  return (
    <SafeAreaView style={styles.safe}>
      <StatusBar barStyle="dark-content" backgroundColor="#F0F4FB" />

      {/* ── Header ── */}
      <View style={styles.header}>
        <TouchableOpacity style={styles.backBtn} onPress={onBack} activeOpacity={0.7}>
          <Text style={styles.backChevron}>‹</Text>
        </TouchableOpacity>

        <Text style={styles.title}>Order Details</Text>

        <TouchableOpacity style={styles.supportBtn} onPress={onSupport} activeOpacity={0.7}>
          <Text style={styles.supportLabel}>🎧 Support</Text>
        </TouchableOpacity>
      </View>

      {/* ── Content ── */}
      <ScrollView
        style={styles.scroll}
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
      >
        <OrderHeaderCard order={order} />

        <OrderItemsCard items={order.items} />

        <DeliveryInfoCard order={order} />

        <PaymentSummaryCard order={order} />

        <OrderActionSection
          status={order.status}
          onTrackOrder={onTrackOrder}
          onReorder={onReorder}
          onOrderAgain={onOrderAgain}
          onContactSupport={onContactSupport}
        />
      </ScrollView>
    </SafeAreaView>
  );
};

export default OrderDetailsScreen;

const styles = StyleSheet.create({
  safe: {
    flex: 1,
    backgroundColor: '#F0F4FB',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 10,
    backgroundColor: '#F0F4FB',
  },
  backBtn: {
    width: 38,
    height: 38,
    borderRadius: 11,
    backgroundColor: '#FFFFFF',
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.07,
    shadowRadius: 4,
    elevation: 2,
  },
  backChevron: {
    fontSize: 26,
    color: '#1E4ED8',
    fontWeight: '300',
    lineHeight: 30,
    marginTop: -2,
  },
  title: {
    flex: 1,
    textAlign: 'center',
    fontSize: 17,
    fontWeight: '700',
    color: '#111827',
    letterSpacing: 0.3,
  },
  supportBtn: {
    paddingHorizontal: 10,
    paddingVertical: 7,
    borderRadius: 10,
    backgroundColor: '#EFF6FF',
  },
  supportLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: '#1E4ED8',
  },
  scroll: {
    flex: 1,
  },
  content: {
    paddingHorizontal: 16,
    paddingTop: 4,
    paddingBottom: 36,
    gap: 12,
  },
});
