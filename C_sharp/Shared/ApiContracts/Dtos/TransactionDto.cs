using System;

namespace ApiContracts.Dtos;

public class TransactionDto
{
public  int Id {get;set;}
public  DateTime Date {get;set;}
public  int ShoppingCartId {get;set;}
public  int CustomerId {get;set;}
}
