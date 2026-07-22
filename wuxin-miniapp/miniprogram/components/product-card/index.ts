Component({
  properties: {
    product: { type: Object, value: {} },
    compact: { type: Boolean, value: false },
    showAdd: { type: Boolean, value: true }
  },
  methods: {
    handleTap() {
      const product = this.data.product as { productId?: number };
      this.triggerEvent('select', { id: product.productId });
    },
    handleAdd() {
      const product = this.data.product as { productId?: number; canAdd?: boolean };
      if (!product.productId || product.canAdd === false) {
        return;
      }
      this.triggerEvent('add', { id: product.productId });
    },
    handleImageError() {
      const product = this.data.product as { productId?: number };
      this.triggerEvent('imageerror', { id: product.productId });
    }
  }
});
