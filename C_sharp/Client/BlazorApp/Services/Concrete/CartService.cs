using BlazorApp.Models;
using BlazorApp.Services.Interface;
using System.Collections.Generic;
using System.Linq;

namespace BlazorApp.Services.Concrete
{
    public class CartService : ICartService
    {
        private readonly List<CartItemViewModel> _items = new();

        public IReadOnlyList<CartItemViewModel> Items => _items;

        public int TotalQuantity => _items.Sum(i => i.Quantity);

        public double TotalPrice => _items.Sum(i => i.Price * i.Quantity);

        public void AddItem(int productId, string name, double price, string? imageUrl)
        {
            var existing = _items.FirstOrDefault(i => i.ProductId == productId);
            if (existing is not null)
            {
                existing.Quantity++;
            }
            else
            {
                _items.Add(new CartItemViewModel
                {
                    ProductId = productId,
                    Name = name,
                    Price = price,
                    ImageUrl = imageUrl,
                    Quantity = 1
                });
            }
        }

        public void RemoveItem(int productId)
        {
            var existing = _items.FirstOrDefault(i => i.ProductId == productId);
            if (existing is not null)
            {
                _items.Remove(existing);
            }
        }

        public void Clear()
        {
            _items.Clear();
        }

        public void SetQuantity(int productId, int quantity)
        {
            var existing = _items.FirstOrDefault(i => i.ProductId == productId);
            if (existing is not null)
            {
                existing.Quantity = quantity < 1 ? 1 : quantity;
            }
        }
    }
}
