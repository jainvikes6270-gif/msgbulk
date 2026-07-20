-- LATHAEPS SMART v3.23.36 • one-time Custom Days validity upgrade
-- Existing project: paste this complete file in Supabase SQL Editor and Run once.
-- This migration DOES NOT change or rotate the saved admin password.

alter table public.app_subscriptions drop constraint if exists app_subscriptions_plan_check;
alter table public.app_subscriptions add constraint app_subscriptions_plan_check
check (plan in ('trial','yearly','custom','lifetime','blocked'));

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
    insert into public.app_subscriptions(device_id) values (v_device) on conflict (device_id) do nothing;
    select * into v_row from public.app_subscriptions where device_id=v_device;
    v_until := case
        when v_row.plan='trial' then v_row.trial_started_at + interval '12 days'
        when v_row.plan in ('yearly','custom') then v_row.expires_at
        when v_row.plan='lifetime' then null
        else v_now
    end;
    v_allowed := v_row.enabled and case
        when v_row.plan='lifetime' then true
        when v_row.plan in ('trial','yearly','custom') then v_until is not null and v_now<v_until
        else false
    end;
    v_message := case
        when v_row.plan='blocked' or not v_row.enabled then 'Access blocked • LATHAEPS se contact karein'
        when v_allowed then 'Online subscription verified'
        else 'Plan expired • Payment required'
    end;
    return jsonb_build_object('device_id',v_device,'plan',v_row.plan,'allowed',v_allowed,
        'access_until_ms',case when v_until is null then 0 else floor(extract(epoch from v_until)*1000)::bigint end,
        'server_time_ms',floor(extract(epoch from v_now)*1000)::bigint,'message',v_message);
end;
$$;

create or replace function public.admin_extend_subscription(p_device_id text,p_days integer,p_admin_password text)
returns jsonb
language plpgsql
security definer
set search_path = public, private, extensions, pg_temp
as $$
declare
    v_device text := upper(regexp_replace(coalesce(p_device_id,''), '[^A-Za-z0-9]', '', 'g'));
    v_days integer := coalesce(p_days,0);
    v_hash text;
    v_current public.app_subscriptions%rowtype;
    v_base timestamptz;
    v_until timestamptz;
begin
    select secret_hash into v_hash from private.app_secrets where secret_name='admin_password';
    if v_hash is null or extensions.crypt(coalesce(p_admin_password,''),v_hash)<>v_hash then
        perform pg_sleep(1);raise exception 'Wrong admin password';
    end if;
    if v_device !~ '^[A-F0-9]{12}$' then raise exception 'Invalid Device ID'; end if;
    if v_days<1 or v_days>365000 then raise exception 'Days must be between 1 and 365000'; end if;
    insert into public.app_subscriptions(device_id) values (v_device) on conflict (device_id) do nothing;
    select * into v_current from public.app_subscriptions where device_id=v_device for update;
    v_base := greatest(clock_timestamp(),case
        when v_current.plan='trial' then v_current.trial_started_at + interval '12 days'
        when v_current.plan in ('yearly','custom') then v_current.expires_at
        else clock_timestamp()
    end);
    v_until := v_base + make_interval(days=>v_days);
    update public.app_subscriptions set plan='custom',enabled=true,expires_at=v_until,updated_at=clock_timestamp()
     where device_id=v_device;
    return jsonb_build_object('ok',true,'device_id',v_device,'plan','custom','days_added',v_days,
        'access_until_ms',floor(extract(epoch from v_until)*1000)::bigint,
        'message',v_days||' days validity extended online');
end;
$$;

revoke all on function public.register_or_get_subscription(text) from public;
revoke all on function public.admin_extend_subscription(text,integer,text) from public;
grant execute on function public.register_or_get_subscription(text) to anon, authenticated;
grant execute on function public.admin_extend_subscription(text,integer,text) to anon, authenticated;
