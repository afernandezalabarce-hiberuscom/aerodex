# Module `:shared:data:datasources`

## Role
Expert KMP dev. Design CRUD contracts (`interface`) over many data origins.

## Rules
- Organize by feature; each feature has one or more datasources by origin.
- Naming:
    - One datasource for all origins: `<Feature>Datasource`.
    - Split by origin when operations differ a lot:
        - Local DB: `<Feature>LocalDatasource`
        - Remote API: `<Feature>RemoteDatasource`
        - Other origin: `<Feature><Other>Datasource` (`Bluetooth`, `Nfc`, `File`...).
- Prefer return types based on `Flow` + `Result`.
- **NO MAGIC VALUES**: Never introduce magic strings or numeric literals; use named constants or typed parameters.
- English for names + comments.
- Always use trailing commas.
- One param per line.

```kotlin
interface UserRemoteDatasource {
    fun getUserById(
        id: String,
    ): Flow<Result<UserDto>>
    fun getAllUsers(): Flow<Result<List<UserDto>>>
    fun createUser(
        user: UserDto,
    ): Flow<Result<Unit>>
    fun updateUser(
        user: UserDto,
    ): Flow<Result<Unit>>
    fun updateUser(
        user: UserDto,
        category: CategoryDto,
    )
    fun deleteUser(
        id: String,
    ): Flow<Result<Unit>>
}
```

## Structure
```text
shared/data/datasources/
├── build.gradle.kts
├── AGENTS.md
└── src/
    └── commonMain/
        └── kotlin/io/afalabarce/template/kmm/data/datasources/
            └── features/
                ├── <FeatureNameA>/
                │   ├── <FeatureNameALocalDatasource.kt>       # local-only access
                │   ├── <FeatureNameABluetoothDatasource.kt>   # Bluetooth-only access
                │   └── <FeatureNameARemoteDatasource.kt>      # remote-only access
                ├── ...                                        # as many features as needed
                └── <FeatureNameB>/
                    └── <FeatureNameBDatasource.kt>            # generic for all access
```
