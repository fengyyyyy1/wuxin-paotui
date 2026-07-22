import { deleteProduct, getProducts, updateProductStatus } from '../../api/catalog';
import { ROUTES } from '../../constants/routes';
import type { Product } from '../../types/product';
import { money } from '../../utils/format';
import { errorMessage } from '../../utils/request';

interface ProductView extends Product { priceText: string; displayImage: string; updating: boolean; }
const DRAFT_KEY = 'WUXIN_MERCHANT_PRODUCT_EDIT';

Page({
  data: { keyword: '', statusIndex: 0, statuses: [{ label: '全部' }, { label: '上架', value: 1 }, { label: '下架', value: 0 }], items: [] as ProductView[], loading: true, loadingMore: false, error: '', pageNum: 1, pages: 1 },
  onShow() { void this.reload(); }, onPullDownRefresh() { void this.reload().finally(() => wx.stopPullDownRefresh()); }, onReachBottom() { if (this.data.pageNum < this.data.pages && !this.data.loadingMore) void this.loadPage(this.data.pageNum + 1, true); },
  onKeyword(event: WechatMiniprogram.Input) { this.setData({ keyword: event.detail.value }); }, onSearch() { void this.reload(); }, onStatus(event: WechatMiniprogram.PickerChange) { this.setData({ statusIndex: Number(event.detail.value) }); void this.reload(); },
  async reload() { this.setData({ items: [], pageNum: 1, pages: 1, error: '' }); await this.loadPage(1, false); },
  async loadPage(pageNum: number, append: boolean) { this.setData(append ? { loadingMore: true } : { loading: true }); try { const status = this.data.statuses[this.data.statusIndex].value; const result = await getProducts({ pageNum, pageSize: 10, productStatus: status, keyword: this.data.keyword.trim() || undefined }); const items = result.records.map(item => ({ ...item, priceText: money(item.price), displayImage: item.productImage || '/assets/images/product-placeholder.svg', updating: false })); this.setData({ items: append ? [...this.data.items, ...items] : items, pageNum: result.pageNum, pages: result.pages, error: '' }); } catch (error) { this.setData({ error: errorMessage(error) }); } finally { this.setData({ loading: false, loadingMore: false }); } },
  add() { wx.removeStorageSync(DRAFT_KEY); wx.navigateTo({ url: ROUTES.productEdit }); }, edit(event: WechatMiniprogram.TouchEvent) { const item = this.data.items.find(value => value.productId === Number(event.currentTarget.dataset.id)); if (!item) return; wx.setStorageSync(DRAFT_KEY, item); wx.navigateTo({ url: `${ROUTES.productEdit}?id=${item.productId}` }); },
  onImageError(event: WechatMiniprogram.ImageError) { const index = Number(event.currentTarget.dataset.index); if (this.data.items[index]?.displayImage !== '/assets/images/product-placeholder.svg') this.setData({ [`items[${index}].displayImage`]: '/assets/images/product-placeholder.svg' }); },
  async toggle(event: WechatMiniprogram.TouchEvent) { const id = Number(event.currentTarget.dataset.id); const index = this.data.items.findIndex(item => item.productId === id); if (index < 0 || this.data.items[index].updating) return; const next = this.data.items[index].productStatus === 1 ? 0 : 1; const confirm = await wx.showModal({ title: next === 1 ? '上架商品' : '下架商品', content: next === 1 ? '上架要求分类启用且库存大于0。' : '下架后用户将无法购买该商品。', confirmColor: next === 1 ? '#18a660' : '#e5484d' }); if (!confirm.confirm) return; this.setData({ [`items[${index}].updating`]: true }); try { await updateProductStatus(id, next); await this.reload(); } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); this.setData({ [`items[${index}].updating`]: false }); } },
  async remove(event: WechatMiniprogram.TouchEvent) { const id = Number(event.currentTarget.dataset.id); const confirm = await wx.showModal({ title: '删除商品', content: '删除后商品会逻辑删除并下架，确定继续？', confirmColor: '#e5484d' }); if (!confirm.confirm) return; try { await deleteProduct(id); await this.reload(); wx.showToast({ title: '商品已删除', icon: 'success' }); } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); } }
});
