using System;
using BlazorApp.Models;

namespace BlazorApp.Services.Interface;

public interface ICartService
{
  public IReadOnlyList<CartItemViewModel> Items { get; }
   public int TotalQuantity { get; }
   public double TotalPrice { get; }

   public void AddItem(int productId, string name, double price, string? imageUrl);
public void RemoveItem(int productId);
public void Clear();
  public void SetQuantity(int productId, int quantity);
}
