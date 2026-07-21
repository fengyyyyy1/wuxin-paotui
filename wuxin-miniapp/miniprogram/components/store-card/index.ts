Component({
  properties: {
    store: { type: Object, value: {} }
  },
  methods: {
    handleTap() {
      const store = this.data.store as { storeId?: number };
      this.triggerEvent('select', { id: store.storeId });
    },
    handleImageError() {
      const store = this.data.store as { storeId?: number };
      this.triggerEvent('imageerror', { id: store.storeId });
    }
  }
});
