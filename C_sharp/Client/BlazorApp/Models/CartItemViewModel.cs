using System;

namespace BlazorApp.Models;

public class CartItemViewModel
{
  public int ProductId { get; set; }
    public string Name { get; set; } = "";
    public double Price { get; set; }   
    public int Quantity { get; set; } = 1;
    public string? ImageUrl { get; set; }
}
