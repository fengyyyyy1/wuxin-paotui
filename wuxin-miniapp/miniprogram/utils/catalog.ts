import type { ProductItem } from '../types/product';
import type { StoreListItem } from '../types/store';
import { formatMoney } from './format';
import { DEFAULT_PRODUCT_IMAGE, DEFAULT_STORE_IMAGE, normalizeImageUrl } from './image';

export interface StoreCardView extends StoreListItem {
  imageUrl: string;
  statusText: string;
  timeText: string;
  addressText: string;
}

export interface ProductCardView extends ProductItem {
  storeId?: number;
  storeName?: string;
  imageUrl: string;
  priceText: string;
  originalPriceText: string;
  stockText: string;
  salesText: string;
  discountText: string;
  canAdd: boolean;
}

export function toStoreCard(store: StoreListItem): StoreCardView {
  return {
    ...store,
    imageUrl: normalizeImageUrl(store.storeLogo, DEFAULT_STORE_IMAGE),
    statusText: store.businessStatusText || (store.businessStatus === 1 ? '营业中' : '休息中'),
    timeText: store.openTime && store.closeTime
      ? `${store.openTime.slice(0, 5)}-${store.closeTime.slice(0, 5)}`
      : '营业时间待补充',
    addressText: [store.district, store.detailAddress].filter(Boolean).join('')
  };
}

export function toProductCard(
  product: ProductItem,
  options: { storeId?: number; storeName?: string; canSell?: boolean } = {}
): ProductCardView {
  const originalPrice = Number(product.originalPrice || 0);
  const price = Number(product.price || 0);
  const hasDiscount = originalPrice > price && price > 0;
  return {
    ...product,
    storeId: options.storeId,
    storeName: options.storeName,
    imageUrl: normalizeImageUrl(product.productImage, DEFAULT_PRODUCT_IMAGE),
    priceText: formatMoney(product.price),
    originalPriceText: hasDiscount ? formatMoney(product.originalPrice) : '',
    stockText: product.stock > 0 ? `库存 ${product.stock}` : '已售罄',
    salesText: typeof product.sales === 'number' ? `已售 ${product.sales}` : '',
    discountText: hasDiscount ? '优惠' : '',
    canAdd: (options.canSell ?? true) && product.stock > 0
  };
}

export function replaceStoreImage(stores: StoreCardView[], storeId: number): StoreCardView[] {
  return stores.map((store) => store.storeId === storeId ? { ...store, imageUrl: DEFAULT_STORE_IMAGE } : store);
}

export function replaceProductImage(products: ProductCardView[], productId: number): ProductCardView[] {
  return products.map((product) =>
    product.productId === productId ? { ...product, imageUrl: DEFAULT_PRODUCT_IMAGE } : product
  );
}
