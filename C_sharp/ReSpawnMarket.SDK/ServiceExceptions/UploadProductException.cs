using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.ServiceExceptions;

public class UploadProductException : Exception
{
    public UploadProductException(string message) : base(message)
    {
    }
}
