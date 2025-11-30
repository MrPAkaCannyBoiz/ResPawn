using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.ServiceExceptions;

public class InvalidLoginException : Exception
{
    public InvalidLoginException(string message) : base(message)
    {
    }
}
