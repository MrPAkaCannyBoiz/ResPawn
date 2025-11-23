package org.example.respawnmarket.Service;

import com.google.protobuf.Timestamp;
import com.respawnmarket.*;
import io.grpc.stub.StreamObserver;
import org.example.respawnmarket.entities.*;
import org.example.respawnmarket.entities.enums.ApprovalStatusEnum;
import org.example.respawnmarket.entities.enums.CategoryEnum;
import org.example.respawnmarket.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
@Service
public class PurchaseServiceImpl extends PurchaseServiceGrpc.PurchaseServiceImplBase
{
  private CustomerRepository customerRepository;
  private ProductRepository productRepository;
  private ShoppingCartRepository shoppingCartRepository;
  private CartProductRepository cartProductRepository;
  private TransactionRepository transactionRepository;
@Autowired
  public PurchaseServiceImpl(CustomerRepository customerRepository,
      ProductRepository productRepository,
      ShoppingCartRepository shoppingCartRepository,
      CartProductRepository cartProductRepository,
      TransactionRepository transactionRepository)
  {
    this.customerRepository = customerRepository;
    this.productRepository = productRepository;
    this.shoppingCartRepository = shoppingCartRepository;
    this.cartProductRepository = cartProductRepository;
    this.transactionRepository = transactionRepository;
  }
  public void buyProducts(BuyProductsRequest request,
      StreamObserver<BuyProductsResponse> responseObserver)
  {
    // get customer
    CustomerEntity customer = customerRepository.findById(request.getCustomerId()).orElseThrow(() -> new RuntimeException("Customer not found" + request.getCustomerId()));
    // load product and process prices
    double totalPrice = 0.0;
    List<CartProductEntity> cartProductEntities  = new ArrayList<>();
    List<ProductEntity> purchasedProducts  = new ArrayList<>();
    for(CartItem item : request.getItemsList())
    {
      ProductEntity product = productRepository.findById(item.getProductId()).orElseThrow(() -> new RuntimeException("Product not found" + item.getProductId()));
      if (product.isSold())
      {
        throw new RuntimeException("Product already sold: " + item.getProductId());
      }
      int quantity = item.getQuantity();
      totalPrice += product.getPrice() * quantity;
      purchasedProducts.add(product);
    }
  // create and save ShoppingCart
    ShoppingCartEntity cart  = new ShoppingCartEntity();
    cart.setTotalPrice(totalPrice);
    ShoppingCartEntity savedCart =  shoppingCartRepository.save(cart);

    // create CartProduct entries
    for(CartItem item : request.getItemsList())
    {
      ProductEntity product = productRepository.findById(item.getProductId()).orElseThrow(() -> new RuntimeException("Product not found" + item.getProductId()));
      CartProductEntity cpe = new CartProductEntity(savedCart, product, item.getQuantity());
      cartProductRepository.save(cpe);
      cartProductEntities.add(cpe);
      //set product as sold
      product.setSold(true);
      productRepository.save(product);
    }
    // create transaction which will be used to return to customer
    TransactionEntity tx = new TransactionEntity(LocalDateTime.now(), savedCart);
    tx.setCustomerId(customer);
      TransactionEntity savedTx = transactionRepository.save(tx);

      // map enteties to proto
    ShoppingCart cartDto = ShoppingCart.newBuilder()
        .setId(savedCart.getId())
        .setTotalPrice(savedCart.getTotalPrice())
        .build();

    List<CartProduct> cartProductDtos = cartProductEntities.stream()
        .map(cpe -> CartProduct.newBuilder()
            .setCartId(cpe.getCart().getId())
            .setProductId(cpe.getProduct().getId())
            .setQuantity(cpe.getQuantity())
            .build())
        .toList();

    List<Product> productDtos =  purchasedProducts.stream()
        .map(this::mapProductToProto)
        .toList();

   Timestamp timestamp = toTimestamp(savedTx.getDate());
    Transaction txDto = Transaction.newBuilder()
        .setId(savedTx.getId())
        .setDate(timestamp)
        .setShoppingCartId(savedCart.getId())
        .setCustomerId(customer.getId())
        .build();

    BuyProductsResponse response = BuyProductsResponse.newBuilder()
        .setTransaction(txDto)
        .setShoppingCart(cartDto)
        .addAllCartProducts(cartProductDtos)
        .addAllPurchasedProducts(productDtos)
        .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();

  }
  private Timestamp toTimestamp(LocalDateTime dateTime) {
    return Timestamp.newBuilder()
        .setSeconds(dateTime.atZone(ZoneId.systemDefault()).toEpochSecond())
        .setNanos(dateTime.getNano())
        .build();
  }

  private Product mapProductToProto(ProductEntity p) {
    return Product.newBuilder()
        .setId(p.getId())
        .setPrice(p.getPrice())
        .setSold(p.isSold())
        .setCondition(p.getCondition())
        .setApprovalStatus(mapApprovalStatus(p.getApprovalStatus()))
        .setName(p.getName())
        .setPhotoUrl(p.getPhotoUrl() == null ? "" : p.getPhotoUrl())
        .setCategory(mapCategory(p.getCategory()))
        .setDescription(p.getDescription())
        .setSoldByCustomerId(p.getSeller().getId())
        .setRegisterDate(toTimestamp(p.getRegisterDate()))
        .setOtherCategory(p.getOtherCategory() == null ? "" : p.getOtherCategory())
        .setPawnshopId(p.getPawnshop() == null ? 0 : p.getPawnshop().getId())
        .build();
  }

  private ApprovalStatus mapApprovalStatus(ApprovalStatusEnum status) {
    return ApprovalStatus.valueOf(status.name());
  }

  private Category mapCategory(CategoryEnum categoryEnum) {
    return Category.valueOf(categoryEnum.name());
  }
}
