import { STORAGE_KEYS } from '../constants/storage';
import type { StoreCardView } from '../utils/catalog';

const HISTORY_LIMIT = 10;
const RECENT_STORE_LIMIT = 6;

export function getSearchHistory(): string[] {
  const value = wx.getStorageSync(STORAGE_KEYS.searchHistory);
  return Array.isArray(value) ? value.filter((item): item is string => typeof item === 'string') : [];
}

export function saveSearchKeyword(keyword: string): string[] {
  const normalized = keyword.trim();
  if (!normalized) return getSearchHistory();
  const history = [normalized, ...getSearchHistory().filter((item) => item !== normalized)].slice(0, HISTORY_LIMIT);
  wx.setStorageSync(STORAGE_KEYS.searchHistory, history);
  return history;
}

export function clearSearchHistory(): void {
  wx.removeStorageSync(STORAGE_KEYS.searchHistory);
}

export function getRecentStores(): StoreCardView[] {
  const value = wx.getStorageSync(STORAGE_KEYS.recentStores);
  return Array.isArray(value) ? value as StoreCardView[] : [];
}

export function saveRecentStore(store: StoreCardView): StoreCardView[] {
  const stores = [store, ...getRecentStores().filter((item) => item.storeId !== store.storeId)]
    .slice(0, RECENT_STORE_LIMIT);
  wx.setStorageSync(STORAGE_KEYS.recentStores, stores);
  return stores;
}
