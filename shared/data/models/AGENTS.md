# Module `:shared:data:models`

## Role
Expert KMP dev. Strong in:
- DB design with [Room](https://developer.android.com/training/data-storage/room) + SQL optimization.
    - Deep [SQLite](https://sqlite.org/docs.html) knowledge.
    - Referential integrity + cascade updates.
    - Query/table optimization; add indexes for auto-generated queries.
    - Normalize to [3NF](https://www.datacamp.com/es/tutorial/third-normal-form), and [Boyce-Codd](https://es.wikipedia.org/wiki/Forma_normal_de_Boyce-Codd) if needed.
- DTO design for external services.
    - REST via [Ktorfit](https://foso.github.io/Ktorfit/).
    - Every DTO `data class` annotated `@Serializable` (`kotlinx.serialization.Serializable`).
    - Every DTO field annotated `@SerialName` (even if the name matches exactly).

## Code organization
- All source under `commonMain`.
- Organize by feature + destination.
- Features named in camelCase.
- Each feature has as many entities of each type as needed.

```text
shared/data/models/
├── build.gradle.kts
├── AGENTS.md
└── src/
    └── commonMain/
        └── kotlin/io/afalabarce/template/kmm/data/models/
            └── features/
                ├── <FeatureNameA>/
                │   ├── db/
                │   │   └── <FeatureNameA>Entity.kt      # DB entity
                │   ├── remote/
                │   │   └── <FeatureNameA>Dto.kt         # transfer DTO
                │   └── bluetooth/                       # OPTIONAL, only if explicitly requested
                │       └── <FeatureNameA>Bt.kt          # Bluetooth DTO
                ├── ...                                  # as many features as needed
                └── <FeatureNameB>/
                    ├── db/
                    │   └── <FeatureNameB>Entity.kt
                    ├── remote/
                    │   └── <FeatureNameB>Dto.kt
                    └── bluetooth/                       # OPTIONAL
                        └── <FeatureNameB>Bt.kt
```

## Rules
- English for names + comments.
- **NO MAGIC VALUES**: Avoid magic strings or numbers; use named constants for table/column/index identifiers when shared or repeated.
- Annotate every field; one annotation per line, separated from the field.

### Room
- Entities: singular, PascalCase (`Invoice`, `Customer`).
- Table names: plural snake_case from entity (`InvoiceDetail` → `invoice_details`).
- Foreign key → auto-create an index wrapping it, unless the FK is a composite primary key.
- Naming prefixes: `IX_` index, `UQ_` unique index, `FK_` foreign key. An FK column is named exactly like the key it references.
- Complex entities = [POJO](https://es.wikipedia.org/wiki/Plain_Old_Java_Object)s built from simple entities, suffixed `Pojo`.

```kotlin
// standard table
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("customer_id")
    val customerId: Long,
    @ColumnInfo("customer_name")
    val customerName: String,
    @ColumnInfo("customer_email")
    val customerEmail: String,
)

// standard table
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("product_id")
    val productId: Long,
    @ColumnInfo("product_name")
    val productName: String,
    @ColumnInfo("product_price")
    val productPrice: Double
)

// entity with FKs, common + unique indexes
@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["customer_id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = true,
            name = "FK_invoices_customers"
        )
    ],
    indices = [
        Index(
            name = "IX_invoices_customer_id",
            value = ["customer_id"]
        ),
        Index(
            name = "UQ_serial_number_data",
            value = [
                "invoice_serial",
                "invoice_number",
                "invoice_date"
            ],
            unique = true
        )
    ]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("invoice_id")
    val invoiceId: Long,
    @ColumnInfo("invoice_serial")
    val invoiceSerial: String,
    @ColumnInfo("invoice_number")
    val invoiceNumber: Long,
    @ColumnInfo("invoice_date")
    val invoiceDate: LocalDate,
    @ColumnInfo("customer_id")
    val customerId: Long,
)

@Entity(
    tableName = "invoice_details",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["invoice_id"],
            childColumns = ["invoice_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = true,
            name = "FK_invoice_details_invoices"
        )
    ],
    indices = [
        Index(
            name = "IX_invoice_details_invoice_id",
            value = ["invoice_id"]
        )
    ])
data class InvoiceDetailEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("invoice_details_id")
    val invoiceDetailsId: Long,
    @ColumnInfo("invoice_id")
    val invoiceId: Long,
    @ColumnInfo("quantity")
    val quantity: Double,
    @ColumnInfo("price")
    val price: Double,
)

// entity with FKs and composite primary key
@Entity(tableName="invoice_details_products",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceDetailEntity::class,
            parentColumns = ["invoice_details_id"],
            childColumns = ["invoice_details_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = true,
            name = "FK_invoice_details_products_invoice_details"
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["product_id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = true,
            name = "FK_invoice_details_products_products"
        )
    ],
    indices = [
        Index(
            name = "IX_invoice_details_products_invoice_details_id",
            value = ["invoice_details_id"]
        ),
        Index(
            name = "IX_invoice_details_products_product_id",
            value = ["product_id"]
        )
    ],
    primaryKeys = [
        "invoice_details_id",
        "product_id"
    ]
)
data class InvoiceDetailsProduct(
    @ColumnInfo("invoice_details_id")
    val invoiceDetailsId: Long,
    @ColumnInfo("product_id")
    val productId: Long,
    @ColumnInfo("quantity")
    val quantity: Double,
    @ColumnInfo("price")
    val price: Double,
)

// POJO entities
data class InvoiceDetailPojo(
    @Embebbed
    val invoiceDetails: InvoiceDetailEntity,
    @Relation(
        parentColumn = "invoice_details_id",
        entityColumn = "invoice_details_id"
    )
    val products: List<InvoiceDetailsProduct>
)

data class Invoice(
    @Embebbed
    val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "customer_id",
        entityColumn = "customer_id"
    )
    val customer: CustomerEntity,
    @Relation(
        entity = InvoiceDetailEntity::class,
        parentColumn = "invoice_id",
        entityColumn = "invoice_id"
    )
    val invoiceDetails: List<InvoiceDetailPojo>
)
```

### Ktorfit / Bluetooth / etc
```kotlin
@Serializable
data class ExampleDto(
    @SerialName("example_field")
    val exampleField: String
)
```
