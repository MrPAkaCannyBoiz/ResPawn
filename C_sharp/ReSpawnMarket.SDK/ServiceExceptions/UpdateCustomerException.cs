using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.ServiceExceptions;

public class UpdateCustomerException : Exception
{
    public UpdateCustomerException(string message) : base(message)
    {
    }
}
