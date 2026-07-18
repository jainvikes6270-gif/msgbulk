-- LATHAEPS SMART v3.23.33 secure subscription setup
-- Paste this complete file into Supabase SQL Editor and press Run once.
-- IMPORTANT: the last result displays a newly generated admin password. Save it privately.

create schema if not exists extensions;
create extension if not exists pgcrypto with schema extensions;
alter extension pgcrypto set schema extensions;
create schema if not exists private;

create table if not exists public.app_subscriptions (
    device_id text primary key,
    plan text not null default 'trial' check (plan in ('trial','yearly','lifetime','blocked')),
    trial_started_at timestamptz not null default now(),
    expires_at timestamptz,
    enabled boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint valid_device_id check (device_id ~ '^[A-F0-9]{12}$')
);

create table if not exists private.app_secrets (
    secret_name text primary key,
    secret_hash text not null,
    updated_at timestamptz not null default now()
);

alter table public.app_subscriptions enable row level security;
alter table private.app_secrets enable row level security;
revoke all on public.app_subscriptions from anon, authenticated;
revoke all on private.app_secrets from anon, authenticated;

create or replace function public.register_or_get_subscription(p_device_id text)
returns jsonb
language plpgsql
security definer
set search_path = public, pg_temp
as $$
declare
    v_device text := upper(regexp_replace(coalesce(p_device_id,''), '[^A-Za-z0-9]', '', 'g'));
    v_row public.app_subscriptions%rowtype;
    v_now timestamptz := clock_timestamp();
    v_until timestamptz;
    v_allowed boolean;
    v_message text;
begin
    if v_device !~ '^[A-F0-9]{12}$' then raise exception 'Invalid Device ID'; end if;

    insert into public.app_subscriptions(device_id) values (v_device)
    on conflict (device_id) do nothing;
    select * into v_row from public.app_subscriptions where device_id = v_device;

    v_until := case
        when v_row.plan = 'trial' then v_row.trial_started_at + interval '12 days'
        when v_row.plan = 'yearly' then v_row.expires_at
        when v_row.plan = 'lifetime' then null
        else v_now
    end;
    v_allowed := v_row.enabled and case
        when v_row.plan = 'lifetime' then true
        when v_row.plan in ('trial','yearly') then v_until is not null and v_now < v_until
        else false
    end;
    v_message := case
        when v_row.plan = 'blocked' or not v_row.enabled then 'Access blocked • LATHAEPS se contact karein'
        when v_allowed then 'Online subscription verified'
        else 'Plan expired • Payment required'
    end;

    return jsonb_build_object(
        'device_id', v_device,
        'plan', v_row.plan,
        'allowed', v_allowed,
        'access_until_ms', case when v_until is null then 0 else floor(extract(epoch from v_until) * 1000)::bigint end,
        'server_time_ms', floor(extract(epoch from v_now) * 1000)::bigint,
        'message', v_message
    );
end;
$$;

create or replace function public.admin_set_subscription(p_device_id text, p_plan text, p_admin_password text)
returns jsonb
language plpgsql
security definer
set search_path = public, private, extensions, pg_temp
as $$
declare
    v_device text := upper(regexp_replace(coalesce(p_device_id,''), '[^A-Za-z0-9]', '', 'g'));
    v_plan text := lower(trim(coalesce(p_plan,'')));
    v_hash text;
begin
    select secret_hash into v_hash from private.app_secrets where secret_name = 'admin_password';
    if v_hash is null or extensions.crypt(coalesce(p_admin_password,''), v_hash) <> v_hash then
        perform pg_sleep(1);
        raise exception 'Wrong admin password';
    end if;
    if v_device !~ '^[A-F0-9]{12}$' then raise exception 'Invalid Device ID'; end if;
    if v_plan not in ('yearly','lifetime','blocked') then raise exception 'Invalid plan'; end if;

    insert into public.app_subscriptions(device_id) values (v_device)
    on conflict (device_id) do nothing;
    update public.app_subscriptions
       set plan = v_plan,
           enabled = (v_plan <> 'blocked'),
           expires_at = case when v_plan = 'yearly' then clock_timestamp() + interval '365 days' else null end,
           updated_at = clock_timestamp()
     where device_id = v_device;

    return jsonb_build_object(
        'ok', true, 'device_id', v_device, 'plan', v_plan,
        'message', case
            when v_plan = 'yearly' then '1 Year plan activated online'
            when v_plan = 'lifetime' then 'Lifetime Free activated online'
            else 'Device access blocked'
        end
    );
end;
$$;

revoke all on function public.register_or_get_subscription(text) from public;
revoke all on function public.admin_set_subscription(text,text,text) from public;
grant execute on function public.register_or_get_subscription(text) to anon, authenticated;
grant execute on function public.admin_set_subscription(text,text,text) to anon, authenticated;

-- Generate a strong random admin password. Running the entire script again rotates it.
with generated as (
    select encode(extensions.gen_random_bytes(24), 'hex') as admin_password
), saved as (
    insert into private.app_secrets(secret_name, secret_hash, updated_at)
    select 'admin_password', extensions.crypt(admin_password, extensions.gen_salt('bf', 12)), now()
      from generated
    on conflict (secret_name) do update
      set secret_hash = excluded.secret_hash, updated_at = excluded.updated_at
    returning secret_name
)
select generated.admin_password as "SAVE_THIS_ADMIN_PASSWORD"
from generated cross join saved;
