import { createCategory, deleteCategory, getCategories, updateCategory, updateCategoryStatus } from '../../api/catalog';
import type { Category } from '../../types/product';
import { errorMessage } from '../../utils/request';
Page({
  data: { items: [] as Category[], loading: true, name: '', sort: '0', editingId: 0, submitting: false },
  onShow() { void this.load(); }, onPullDownRefresh() { void this.load().finally(() => wx.stopPullDownRefresh()); },
  async load() { this.setData({ loading: true }); try { this.setData({ items: await getCategories() }); } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); } finally { this.setData({ loading: false }); } },
  onName(event: WechatMiniprogram.Input) { this.setData({ name: event.detail.value }); }, onSort(event: WechatMiniprogram.Input) { this.setData({ sort: event.detail.value }); },
  edit(event: WechatMiniprogram.TouchEvent) { const item = this.data.items.find(value => value.categoryId === Number(event.currentTarget.dataset.id)); if (item) this.setData({ editingId: item.categoryId, name: item.categoryName, sort: String(item.sort || 0) }); }, cancel() { this.setData({ editingId: 0, name: '', sort: '0' }); },
  async save() { const name = this.data.name.trim(); if (!name || this.data.submitting) { wx.showToast({ title: '请输入分类名称', icon: 'none' }); return; } this.setData({ submitting: true }); try { if (this.data.editingId) await updateCategory(this.data.editingId, name, Number(this.data.sort) || 0); else await createCategory(name, Number(this.data.sort) || 0); this.cancel(); await this.load(); wx.showToast({ title: '分类已保存', icon: 'success' }); } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); } finally { this.setData({ submitting: false }); } },
  async toggle(event: WechatMiniprogram.TouchEvent) { const id = Number(event.currentTarget.dataset.id); const status = Number(event.currentTarget.dataset.status) === 1 ? 0 : 1; try { await updateCategoryStatus(id, status); await this.load(); } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); } },
  async remove(event: WechatMiniprogram.TouchEvent) { const id = Number(event.currentTarget.dataset.id); const result = await wx.showModal({ title: '删除分类', content: '分类下存在商品时后端会拒绝删除。', confirmColor: '#e5484d' }); if (!result.confirm) return; try { await deleteCategory(id); await this.load(); wx.showToast({ title: '分类已删除', icon: 'success' }); } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); } }
});
