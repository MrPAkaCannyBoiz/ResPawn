create sequence pawn_shop_id_seq
    as integer;

create sequence product_image_id_seq
    as integer;

create domain string as varchar(300);

create table postal
(
    postal_code integer      not null
        primary key,
    city        varchar(255) not null
);


create table address
(
    id             serial
        primary key,
    street_name    varchar(255) not null,
    secondary_unit varchar(255),
    postal_code    integer      not null
        references postal
);


create table customer
(
    id           serial
        primary key,
    first_name   varchar(255) not null,
    last_name    varchar(255) not null,
    email        varchar(255) not null
        unique
        constraint customer_email_check
            check ((POSITION(('@'::text) IN (email)) > 1) AND
                   (POSITION(('.'::text) IN (email)) > (POSITION(('@'::text) IN (email)) + 1)) AND
                   ((length((email)::text) - length(replace((email)::text, '@'::text, ''::text))) = 1))
        constraint not_empty_email_check
            check (char_length((email)::text) > 0),
    phone_number varchar(255) not null
        unique,
    password     varchar(255) not null
        constraint customer_password_check
            check ((length((password)::text) >= 8) AND ((password)::text ~ '^(?=.*[A-Za-z])(?=.*\d).+$'::text)),
    can_sell     boolean default true,
    constraint not_empty_name_check
        check ((char_length((first_name)::text) > 0) AND (char_length((last_name)::text) > 0))
);


create table reseller
(
    id       serial
        primary key,
    name     varchar(255) not null,
    username varchar(255) not null
        unique,
    password varchar(255)
        constraint reseller_password_check
            check ((length((password)::text) >= 8) AND ((password)::text ~ '^(?=.*[A-Za-z])(?=.*\d).+$'::text))
);


create table shopping_cart
(
    id          serial
        primary key,
    total_price double precision not null
);


create table transaction
(
    id               serial
        primary key,
    date             timestamp(6) not null,
    shopping_cart_id integer      not null
        references shopping_cart,
    customer_id      integer
        constraint transaction_paid_by_customer_id_fkey
            references customer
);


create table customer_address
(
    customer_id integer not null
        references customer,
    address_id  integer not null
        references address,
    primary key (customer_id, address_id)
);


create table pawnshop
(
    id         integer default nextval('pawn_shop_id_seq'::regclass) not null
        constraint pawn_shop_pkey
            primary key,
    name       varchar(255)                                          not null,
    address_id integer                                               not null
        constraint pawn_shop_address_id_fkey
            references address
);


alter sequence pawn_shop_id_seq owned by pawnshop.id;

create table product
(
    id               serial
        primary key,
    price            double precision
        constraint product_price_non_negative
            check (price >= (0)::double precision),
    sold             boolean default false not null,
    condition        varchar(255),
    approval_status  varchar(255)          not null
        constraint product_approval_status_check
            check ((approval_status)::text = ANY
                   ((ARRAY ['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'REVIEWING'::character varying])::text[])),
    name             varchar(255)          not null,
    category         varchar(255)          not null,
    description      varchar(255)          not null,
    sold_by_customer integer               not null
        references customer,
    register_date    timestamp(6)          not null,
    other_category   varchar(255),
    pawnshop_id      integer default 0
        constraint product_pawn_shop_id_fkey
            references pawnshop
);


create table inspection
(
    id                       serial
        primary key,
    product_id               integer      not null
        references product,
    inspected_by_reseller_id integer      not null
        references reseller,
    inspection_date          timestamp(6) not null,
    comments                 varchar(255),
    is_accepted              boolean      not null,
    approval_stage           varchar(255) default 'REVIEWING'::character varying
        constraint approval_stage_check
            check ((approval_stage)::text = ANY
                   ((ARRAY ['APPROVED'::character varying, 'REVIEWING'::character varying, 'approved'::character varying, 'reviewing'::character varying])::text[]))
);


create table cart_product
(
    shopping_cart_id integer not null
        references shopping_cart,
    product_id       integer not null
        references product,
    quantity         integer default 1,
    primary key (shopping_cart_id, product_id)
);


create table stock
(
    quantity   integer not null,
    address_id integer not null
        constraint fk2bsamfwk3wotheqws4wg66vbl
            references address,
    product_id integer not null
        constraint fkjghkvw2snnsr5gpct0of7xfcf
            references product,
    primary key (address_id, product_id)
);


create table image
(
    id         integer default nextval('product_image_id_seq'::regclass) not null
        constraint product_image_pkey
            primary key,
    image_url  varchar(255)                                              not null,
    product_id integer                                                   not null
        constraint product_image_product_id_fkey
            references product
);


alter sequence product_image_id_seq owned by image.id;

create index idx_image_product_id
    on image (product_id);

create function enforce_max_five_images_per_product() returns trigger
    language plpgsql
as
$$
    begin
    if (select count(*) from image where product_id = NEW.product_id) >= 5 then
        raise exception 'Cannot add more than 5 images for a single product';
    end if;
    return NEW;
end; $$;


create trigger check_max_images_per_product
    before insert
    on image
    for each row
execute procedure enforce_max_five_images_per_product();


