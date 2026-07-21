Component({
  properties: {
    value: { type: Number, value: 0 },
    min: { type: Number, value: 0 },
    max: { type: Number, value: 999 },
    disabled: { type: Boolean, value: false },
    loading: { type: Boolean, value: false }
  },
  methods: {
    decrease() {
      if (this.data.disabled || this.data.loading || this.data.value <= this.data.min) return;
      this.triggerEvent('change', { value: this.data.value - 1, delta: -1 });
    },
    increase() {
      if (this.data.disabled || this.data.loading || this.data.value >= this.data.max) return;
      this.triggerEvent('change', { value: this.data.value + 1, delta: 1 });
    }
  }
});
