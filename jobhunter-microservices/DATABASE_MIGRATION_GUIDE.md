# Database Migration Guide

## Overview
This guide helps you migrate from the monolith database to separate microservice databases.

## Database Structure

### Before (Monolith)
```
jobhunter (Single Database)
├── users
├── roles
├── permissions
├── user_role
├── permission_role
├── companies
├── jobs
├── skills
├── job_skill
├── resumes
└── subscribers
```

### After (Microservices)
```
auth_db
├── users
├── roles
├── permissions
├── user_role
└── permission_role

company_db
└── companies

job_db
├── jobs
├── skills
└── job_skill

resume_db
└── resumes

notification_db
└── subscribers
```

## Migration Steps

### Option 1: Fresh Start (Recommended for Development)

1. **Stop all services**
   ```bash
   docker-compose down
   ```

2. **Remove old MySQL volume**
   ```bash
   docker volume rm jobhunter-microservices_mysql_data
   ```

3. **Start MySQL and wait for initialization**
   ```bash
   docker-compose up -d mysql
   # Wait 30 seconds for database initialization
   ```

4. **Run your application migrations**
   Each microservice will create its own tables using JPA/Hibernate when it starts.

5. **Start all services**
   ```bash
   docker-compose up -d
   ```

### Option 2: Migrate Existing Data

1. **Backup your current database**
   ```bash
   docker exec jobhunter-mysql mysqldump -uroot -proot jobhunter > backup_monolith.sql
   ```

2. **Start services to create new databases**
   ```bash
   docker-compose up -d mysql
   # Wait for initialization
   ```

3. **Run migration script**
   ```bash
   docker exec -i jobhunter-mysql mysql -uroot -proot < docker/mysql/migrate-monolith-to-microservices.sql
   ```

4. **Verify migration**
   ```bash
   docker exec jobhunter-mysql mysql -uroot -proot -e "
   SELECT 'auth_db' AS db, COUNT(*) as users FROM auth_db.users
   UNION ALL
   SELECT 'company_db', COUNT(*) FROM company_db.companies
   UNION ALL
   SELECT 'job_db', COUNT(*) FROM job_db.jobs;
   "
   ```

## Important Notes

### Foreign Key Considerations

When splitting databases, foreign key relationships that cross service boundaries need special handling:

**Example Issues:**
- `jobs.company_id` → references `companies` (now in different DB)
- `resumes.user_id` → references `users` (now in different DB)
- `resumes.job_id` → references `jobs` (now in different DB)

**Solutions:**
1. **Remove DB-level foreign keys** for cross-service references
2. **Implement referential integrity in application code**
3. **Use eventual consistency patterns**
4. **Implement distributed transactions if needed (Saga pattern)**

### Data Consistency

After migration, each service should:
1. ✅ Own its data completely
2. ✅ Never directly access another service's database
3. ✅ Use REST APIs or events to communicate
4. ✅ Cache foreign data locally if needed

## Rollback Plan

If migration fails:

```bash
# Stop all services
docker-compose down

# Restore backup
docker-compose up -d mysql
docker exec -i jobhunter-mysql mysql -uroot -proot jobhunter < backup_monolith.sql

# Switch back to monolith configuration
# (Update docker-compose.yml to use single database)
```

## Testing After Migration

1. **Test each service independently**
   ```bash
   # Auth Service
   curl http://localhost:8081/actuator/health
   
   # Company Service
   curl http://localhost:8082/actuator/health
   
   # Job Service
   curl http://localhost:8083/actuator/health
   ```

2. **Test cross-service operations**
   - Create a job (requires company reference)
   - Submit a resume (requires user and job references)
   - Send notification (requires user reference)

3. **Monitor logs for foreign key errors**
   ```bash
   docker-compose logs -f auth-service company-service job-service
   ```

## Performance Considerations

✅ **Benefits:**
- Better scalability (scale services independently)
- Better isolation (one service's issues don't affect others)
- Easier to optimize per-service databases

⚠️ **Challenges:**
- More complex queries across services
- Need to handle distributed transactions
- Increased network latency for cross-service calls


