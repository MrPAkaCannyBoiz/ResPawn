using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.ServiceExceptions;

public class ProductNotFoundException : Exception
{
    public ProductNotFoundException(string message) : base(message)
    {
    }
}
