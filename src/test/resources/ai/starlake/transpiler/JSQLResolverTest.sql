WITH dat_trt AS (
        SELECT DATE '{{ date_trt }}' AS date_trt )
    , date_parm AS (
        SELECT  CASE
                WHEN (  SELECT  Cast( Format_Date( '%d', date_trt ) AS INTEGER )
                        FROM dat_trt ) <= 10
                    THEN (  SELECT Date_Add( Last_Day( Date_Add( date_trt, INTERVAL -50 DAY ), month ), INTERVAL 1 DAY )
                            FROM dat_trt )
                ELSE (  SELECT Date_Add( Last_Day( Date_Add( date_trt, INTERVAL -34 DAY ), month ), INTERVAL 1 DAY )
                        FROM dat_trt )
                END AS date_debut_periode
                , CASE
                    WHEN (  SELECT  Cast( Format_Date( '%d', date_trt ) AS INTEGER )
                            FROM dat_trt ) <= 10
                        THEN (  SELECT Last_Day( Date_Add( date_trt, INTERVAL -15 DAY ), month )
                                FROM dat_trt )
                    ELSE (  SELECT Last_Day( date_trt )
                            FROM dat_trt )
                    END AS date_fin_periode )
    , step_ref_fact AS (
        SELECT DISTINCT
            Upper( application ) AS application
            , id_client AS id_client
            , id_metier AS id_metier
        FROM flux_ref.ref_fact_client
        WHERE dat_deb_client <= (   SELECT date_debut_periode
                                    FROM date_parm )
            AND dat_fin_client >= ( SELECT date_fin_periode
                                    FROM date_parm ) )
    , step_id_pfe1 AS (
        SELECT DISTINCT
            cod_idt_etb_do AS id_metier_agr_pfe
        FROM "FLUX_PFE"."AGR_PFE_MINOS_DAY"
        WHERE dat_ref BETWEEN ( SELECT date_debut_periode
                                FROM date_parm )
                             AND (  SELECT date_fin_periode
                                    FROM date_parm )
            AND cod_sens_side = 'E'
        UNION ALL
        SELECT DISTINCT
            cod_idt_etb_des AS id_metier_agr_pfe
        FROM "FLUX_PFE"."AGR_PFE_MINOS_DAY"
        WHERE dat_ref BETWEEN ( SELECT date_debut_periode
                                FROM date_parm )
                             AND (  SELECT date_fin_periode
                                    FROM date_parm )
            AND cod_sens_side = 'R'
        UNION ALL
        SELECT DISTINCT
            cod_etab_eme AS id_metier_agr_pfe
        FROM "FLUX_PFE"."AGR_PFE_SEPA"
        WHERE dat_ref BETWEEN ( SELECT date_debut_periode
                                FROM date_parm )
                             AND (  SELECT date_fin_periode
                                    FROM date_parm )
            AND cod_sens_side = 'E'
        UNION ALL
        SELECT DISTINCT
            cod_etab_dest AS id_metier_agr_pfe
        FROM "FLUX_PFE"."AGR_PFE_SEPA"
        WHERE dat_ref BETWEEN ( SELECT date_debut_periode
                                FROM date_parm )
                             AND (  SELECT date_fin_periode
                                    FROM date_parm )
            AND cod_sens_side = 'R' )
    , step_id_pfe_all AS (
        SELECT  'PFE' AS perimetre
                , 'PFE' AS application
                , id_metier_agr_pfe AS id_metier
        FROM step_id_pfe1
            LEFT JOIN step_ref_fact
                ON ( step_id_pfe1.id_metier_agr_pfe = step_ref_fact.id_metier
                        AND step_ref_fact.application = 'PFE' )
        WHERE step_ref_fact.id_metier IS NULL )
    , step_id_esp1 AS (
        SELECT DISTINCT
            Ifnull( cod_pres, '_' ) AS id_metier_agr_esp
        FROM "FLUX_ESP"."AGR_MASSE_DAY"
        WHERE dat_ref BETWEEN ( SELECT date_debut_periode
                                FROM date_parm )
                             AND (  SELECT date_fin_periode
                                    FROM date_parm )
            AND sens = 'E'
        UNION ALL
        SELECT DISTINCT
            Ifnull( cod_dest, '_' ) AS id_metier_agr_bip
        FROM "FLUX_ESP"."AGR_MASSE_DAY"
        WHERE dat_ref BETWEEN ( SELECT date_debut_periode
                                FROM date_parm )
                             AND (  SELECT date_fin_periode
                                    FROM date_parm )
            AND sens = 'R' )
    , step_id_esp_all AS (
        SELECT  'ESP' AS perimetre
                , 'ESP' AS application
                , id_metier_agr_esp AS id_metier
        FROM step_id_esp1
            LEFT JOIN step_ref_fact
                ON ( step_id_esp1.id_metier_agr_esp = step_ref_fact.id_metier
                        AND step_ref_fact.application = 'ESP' )
        WHERE step_ref_fact.id_metier IS NULL )
    , step_id_bip1 AS (
        SELECT DISTINCT
            Ifnull( cod_pres, '_' ) AS id_metier_agr_bip
        FROM "FLUX_BIP"."AGR_BIP_DAY"
        WHERE dat_ref BETWEEN ( SELECT date_debut_periode
                                FROM date_parm )
                             AND (  SELECT date_fin_periode
                                    FROM date_parm )
            AND sens = 'E'
        UNION ALL
        SELECT DISTINCT
            Ifnull( cod_dest, '_' ) AS id_metier_agr_bip
        FROM "FLUX_BIP"."AGR_BIP_DAY"
        WHERE dat_ref BETWEEN ( SELECT date_debut_periode
                                FROM date_parm )
                             AND (  SELECT date_fin_periode
                                    FROM date_parm )
            AND sens = 'R' )
    , step_id_bip_all AS (
        SELECT  'BIP' AS perimetre
                , 'ESP' AS application
                , id_metier_agr_bip AS id_metier
        FROM step_id_bip1
            LEFT JOIN step_ref_fact
                ON ( step_id_bip1.id_metier_agr_bip = step_ref_fact.id_metier
                        AND step_ref_fact.application = 'ESP' )
        WHERE step_ref_fact.id_metier IS NULL )
    , tempsw_fact_client_emt AS (
        SELECT  id_metier
                , application
                , groupe
                , cd_bank
                , cd_bank_aux
                , id_client
                , lib_client
        FROM "FLUX_REF"."REF_FACT_CLIENT"
        WHERE ( SELECT date_debut_periode
                FROM date_parm ) BETWEEN dat_deb_client
                                     AND dat_fin_client
            AND application = 'SESAME' )
    , tempsw_fact_client_dst AS (
        SELECT  id_metier
                , application
                , groupe
                , cd_bank
                , cd_bank_aux
                , id_client
                , lib_client
        FROM "FLUX_REF"."REF_FACT_CLIENT"
        WHERE ( SELECT date_debut_periode
                FROM date_parm ) BETWEEN dat_deb_client
                                     AND dat_fin_client
            AND application = 'SESAME' )
    , tempsw_fact_client_ctrp AS (
        SELECT  id_metier
                , application
                , groupe
                , cd_bank
                , cd_bank_aux
                , id_client
                , lib_client
        FROM "FLUX_REF"."REF_FACT_CLIENT"
        WHERE ( SELECT date_debut_periode
                FROM date_parm ) BETWEEN dat_deb_client
                                     AND dat_fin_client
            AND application = 'CONTREPART' )
    , tempsw_fact_qualifiant AS (
        SELECT DISTINCT
            qualifiant
            , groupe_1
            , groupe_2
        FROM "FLUX_REF"."REF_FACT_QUALIFIANT"
        WHERE ( SELECT date_debut_periode
                FROM date_parm ) BETWEEN dat_deb_qualifiant
                                     AND dat_fin_qualifiant
            AND top_bank_identiques IS NULL )
    , tempsw_fact_agr_ini AS (
        SELECT  agr_swt.periode
                , 'SWIFT' filiere
                , 'SESAME' application
                , agr_swt.bic_heb bic_ref
                , NULL cd_bank_ref
                , agr_swt.bic_heb bic_chef_file
                , fgd1.cod_etab cd_bank_chef_file
                , fgd1.cod_cat
                , agr_swt.sens cod_sens
                , agr_swt.msg_type msg_type
                , Trim( agr_swt.CHAMP103 ) cod_canal_rgl
                , CASE Trim( agr_swt.CHAMP103 )
                    WHEN 'ERP'
                        THEN 'Step1'
                    WHEN 'EBA'
                        THEN 'Euro1'
                    WHEN 'TGT'
                        THEN 'Paiement Target2'
                    WHEN 'RTG'
                        THEN 'Paiement Target2'
                    WHEN 'HAM'
                        THEN 'Gestion HAM Target2'
                    WHEN 'CLS'
                        THEN 'CLS'
                    WHEN 'TPS'
                        THEN 'TPS'
                    WHEN 'CLM'
                        THEN 'CLM'
                    ELSE NULL
                    END lib_canal_rgl
                , CASE
                    WHEN Substr( agr_swt.msg_type, 1, 1 ) NOT IN (  '0', '1', '2'
                                                                    , '3', '4', '5'
                                                                    , '6', '7', '8'
                                                                    , '9' )
                        THEN 'SWIFTMX'
                    ELSE 'SWIFTFIN'
                    END canal_paiement
                , CASE
                    WHEN agr_swt.oplog_id IS NULL
                        THEN '_'
                    ELSE agr_swt.oplog_id
                    END cod_unit
                , opl.oplog_lib
                , agr_swt.typevt_id cod_evnt
                , typ.typevt_lib
                , agr_swt.statut_id id_statut
                , stt.stat_lib
                , CASE
                    WHEN agr_swt.sens = 'E'
                        THEN CASE
                                WHEN ( agr_swt.bic_do IS NULL )
                                    THEN agr_swt.bic_heb
                                ELSE agr_swt.bic_do
                                END
                    WHEN agr_swt.sens = 'R'
                        THEN CASE
                                WHEN ( agr_swt.bic_do IS NULL )
                                    THEN agr_swt.bic_cor
                                ELSE agr_swt.bic_do
                                END
                    ELSE '_'
                    END bic_emt
                , CASE
                    WHEN agr_swt.sens = 'E'
                        THEN CASE
                                WHEN agr_swt.oplog_id IS NULL
                                    THEN Concat( agr_swt.bic_heb, '_' )
                                ELSE Concat( agr_swt.bic_heb, agr_swt.oplog_id )
                                END
                    WHEN agr_swt.sens = 'R'
                        THEN CASE
                                WHEN ( agr_swt.bic_do IS NULL )
                                    THEN CASE
                                            WHEN Substr( agr_swt.bic_cor, 1, 4 ) IN ( 'CEPA', 'CCBP', 'NATX' )
                                                THEN agr_swt.bic_cor
                                            ELSE Substr( agr_swt.bic_cor, 1, 8 )
                                            END
                                ELSE CASE
                                        WHEN Substr( agr_swt.bic_do, 1, 4 ) IN ( 'CEPA', 'CCBP', 'NATX' )
                                            THEN agr_swt.bic_do
                                        ELSE Substr( agr_swt.bic_do, 1, 8 )
                                        END
                                END
                    END id_srv_emt
                , CASE sens
                    WHEN 'R'
                        THEN CASE
                                WHEN ( agr_swt.bic_ben IS NULL )
                                    THEN agr_swt.bic_heb
                                ELSE agr_swt.bic_ben
                                END
                    WHEN 'E'
                        THEN CASE
                                WHEN ( agr_swt.bic_ben IS NULL )
                                    THEN agr_swt.bic_cor
                                ELSE agr_swt.bic_ben
                                END
                    ELSE NULL
                    END bic_dst
                , CASE sens
                    WHEN 'R'
                        THEN CASE
                                WHEN agr_swt.oplog_id IS NULL
                                    THEN Concat( agr_swt.bic_heb, '_' )
                                ELSE Concat( agr_swt.bic_heb, agr_swt.oplog_id )
                                END
                    WHEN 'E'
                        THEN CASE
                                WHEN ( agr_swt.bic_ben IS NULL )
                                    THEN CASE
                                            WHEN Substr( agr_swt.bic_cor, 1, 4 ) IN ( 'CEPA', 'CCBP', 'NATX' )
                                                THEN agr_swt.bic_cor
                                            ELSE Substr( agr_swt.bic_cor, 1, 8 )
                                            END
                                ELSE CASE
                                        WHEN Substr( agr_swt.bic_ben, 1, 4 ) IN ( 'CEPA', 'CCBP', 'NATX' )
                                            THEN agr_swt.bic_ben
                                        ELSE Substr( agr_swt.bic_ben, 1, 8 )
                                        END
                                END
                    ELSE NULL
                    END id_srv_dst
                , CASE sens
                    WHEN 'E'
                        THEN 'D'
                    ELSE 'E'
                    END contrepartie
                , 'S/O' tiers_op
                , 'S/O' type_remettant
                , agr_swt.nb_evt quantite
                , agr_swt.cod_dev
                , 0 mnt_dev
                , 0 mnt_eur
        FROM "FLUX_SWIFT"."AGR_SESAME_SWIFT_DAY" agr_swt
            LEFT JOIN "FLUX_REF"."REF_SWIFT_OPLOG" opl
                ON opl.oplog_id = agr_swt.oplog_id
            LEFT JOIN "FLUX_REF"."REF_SWIFT_TYPEVT" typ
                ON typ.typevt_id = agr_swt.typevt_id
            LEFT JOIN "FLUX_REF"."REF_SWIFT_STATUT" stt
                ON stt.stat_id = agr_swt.statut_id
            LEFT JOIN "FLUX_REF"."REF_FIB_FGD" fgd1
                ON fgd1.cod_bic = agr_swt.bic_heb
        WHERE agr_swt.periode BETWEEN ( SELECT date_debut_periode
                                        FROM date_parm )
                                     AND (  SELECT date_fin_periode
                                            FROM date_parm ) )
    , tempsw_fact_agr_tmp1 AS (
        SELECT  fai.periode
                , fai.filiere
                , fai.application
                , fai.bic_ref cod_bic_ref
                ,  Cast( fai.cd_bank_ref AS STRING ) cod_etab_ref
                , CASE
                    WHEN fai.cod_cat IN ( 'BPCE', 'CE', 'BP' )
                        THEN 'BPCEFRPPXXX'
                    ELSE fai.bic_chef_file
                    END bic_chef_file
                , CASE
                    WHEN fai.cod_cat IN ( 'BPCE', 'CE', 'BP' )
                        THEN '16188'
                    ELSE fai.cd_bank_chef_file
                    END cd_bank_chef_file
                , CASE
                    WHEN fai.bic_chef_file = 'BPCEFRPPXXX'
                        THEN 'BPCE'
                    WHEN fai.cod_cat IN ( 'BPCE', 'CE', 'BP' )
                        THEN 'BPCE'
                    ELSE fai.cod_cat
                    END cod_cat
                , fai.cod_sens
                , fai.msg_type
                , fai.cod_canal_rgl
                , fai.lib_canal_rgl
                , fai.canal_paiement
                , '_' lib_sys_rgl
                , '_' ref_op
                , fai.cod_unit
                , fai.oplog_lib lib_unit
                , fai.cod_evnt
                , fai.typevt_lib lib_evnt
                , fai.id_statut
                , fai.stat_lib lib_statut
                , fai.bic_emt
                , fai.id_srv_emt
                , fai.bic_dst
                , fai.id_srv_dst
                , fai.contrepartie
                , fai.tiers_op
                , fai.type_remettant
                , fai.quantite
                , fai.cod_dev cd_devise
                , fai.mnt_dev
                , fai.mnt_eur
                , '_' cd_bank_cl
                , '_' groupe
                , '_' typ_ind
                , CASE
                    WHEN contrepartie = 'D'
                        THEN femt.id_client
                    ELSE fdst.id_client
                    END cod_client_fact
                , '_' cod_typ_op
                , '_' lib_typ_op
                , '_' cod_ent_ges_vb
                , '_' cod_eve_ges_vb
                , CASE
                    WHEN Substr( fai.msg_type, 1, 1 ) IN (  '0', '1', '2'
                                                            , '3', '4', '5'
                                                            , '6', '7', '8'
                                                            , '9' )
                        THEN Concat( 'MT', msg_type )
                    ELSE Concat( 'MX', msg_type )
                    END typ_msg
                , '_' crit_agregat
                , '_' id_srv_ref
                , '_' cod_echange
                , NULL date_echange
                , '_' cod_typ_trt_frais
                , '_' lib_cod_signe_op
                , '_' cod_ope
                , '_' ss_cod_ope
                , '_' lib_ss_cod_ope
                , '_' cod_taxable
                , '_' lib_cod_taxable
                , '_' cd_moyen_pymnt
                , '_' lib_moyen_pymnt
                , '_' frais_trsp_dhl
                , '_' cd_pays_frais_trsp_dhl
                , '_' pays_emt
                , '_' cod_bank_emt
                , '_' lib_etab_emt
                , CASE
                    WHEN femt.groupe IS NULL
                            AND contrepartie = 'D'
                        THEN 'INCONNU'
                    WHEN femt.groupe IS NOT NULL
                            AND contrepartie = 'D'
                        THEN femt.groupe
                    ELSE fctre.groupe
                    END famille_etab_emt
                , '_' pays_dst
                , '_' cod_bank_dst
                , '_' lib_etab_dst
                , CASE
                    WHEN fdst.groupe IS NULL
                            AND contrepartie = 'E'
                        THEN 'INCONNU'
                    WHEN fdst.groupe IS NOT NULL
                            AND contrepartie = 'E'
                        THEN fdst.groupe
                    ELSE fctrd.groupe
                    END famille_etab_dst
                , '_' qualifiant_flux
                , '_' cod_ind_cir
                , NULL crit_supl_1
                , NULL crit_supl_2
                , NULL crit_supl_3
                , NULL crit_supl_4
                , NULL crit_supl_5
                , NULL crit_supl_6
                , NULL crit_supl_7
                , NULL crit_supl_8
                , NULL crit_supl_9
        FROM `tempsw_fact_agr_ini` fai
            LEFT JOIN `tempsw_fact_client_emt` femt
                ON ( femt.id_metier = fai.id_srv_emt
                        AND femt.application = 'SESAME' )
            LEFT JOIN `tempsw_fact_client_dst` fdst
                ON ( fdst.id_metier = fai.id_srv_dst
                        AND fdst.application = 'SESAME' )
            LEFT JOIN `tempsw_fact_client_ctrp` fctre
                ON ( fctre.id_metier = fai.id_srv_emt
                        AND fctre.application = 'CONTREPART' )
            LEFT JOIN `tempsw_fact_client_ctrp` fctrd
                ON ( fctrd.id_metier = fai.id_srv_dst
                        AND fctrd.application = 'CONTREPART' ) )
    , tempsw_fact_agr_tmp2 AS (
        SELECT  periode
                , filiere
                , application
                , Ifnull( tmp1.cod_bic_ref, '_' ) cod_bic_ref
                , Ifnull( tmp1.cod_etab_ref, '_' ) cod_etab_ref
                , bic_chef_file
                , Ifnull( cd_bank_chef_file, '_' ) cd_bank_chef_file
                , cd_bank_cl
                , CASE
                    WHEN contrepartie = 'D'
                        THEN famille_etab_emt
                    ELSE famille_etab_dst
                    END groupe
                , cod_sens
                , msg_type
                , Ifnull( cod_canal_rgl, '_' ) cod_canal_rgl
                , Ifnull( lib_canal_rgl, '_' ) lib_canal_rgl
                , Ifnull( lib_sys_rgl, '_' ) lib_sys_rgl
                , ref_op
                , cod_unit
                , Ifnull( lib_unit, '_' ) lib_unit
                , cod_evnt
                , lib_evnt
                , canal_paiement
                , typ_ind
                , id_statut
                , lib_statut
                , cod_client_fact
                , cd_devise
                , cod_typ_op
                , lib_typ_op
                , cod_typ_trt_frais
                , date_echange
                , lib_cod_signe_op
                , cod_ope
                , ss_cod_ope
                , lib_ss_cod_ope
                , cod_taxable
                , lib_cod_taxable
                , cd_moyen_pymnt
                , lib_moyen_pymnt
                , frais_trsp_dhl
                , cd_pays_frais_trsp_dhl
                , bic_emt
                , Substr( bic_emt, 5, 2 ) pays_emt
                , id_srv_emt
                , cod_bank_emt
                , swfe.lib_inst_name lib_etab_emt
                , famille_etab_emt
                , bic_dst
                , Substr( bic_dst, 5, 2 ) pays_dst
                , id_srv_dst
                , cod_bank_dst
                , swfd.lib_inst_name lib_etab_dst
                , famille_etab_dst
                , CASE
                    WHEN famille_etab_emt = 'TECHNIQUE'
                            OR famille_etab_dst = 'TECHNIQUE'
                        THEN 'TECHNIQUE'
                    WHEN Ifnull( famille_etab_emt, '_' ) = '_'
                            AND contrepartie = 'D'
                        THEN 'INCONNU'
                    WHEN Ifnull( famille_etab_dst, '_' ) = '_'
                            AND contrepartie = 'E'
                        THEN 'INCONNU'
                    WHEN Ifnull( famille_etab_emt, '_' ) = '_'
                            OR Ifnull( famille_etab_dst, '_' ) = '_'
                        THEN 'INTERBANCAIRE'
                    ELSE rfactq.qualifiant
                    END qualifiant_flux
                , contrepartie
                , tiers_op
                , type_remettant
                , quantite
                , mnt_dev
                , mnt_eur
                , cod_ent_ges_vb
                , cod_eve_ges_vb
                , Concat(   Ifnull( cod_sens, '_' ), 'µ', typ_msg
                            , 'µ', Ifnull( cod_canal_rgl, '_' ), 'µ'
                            , Ifnull( cod_unit, '_' ), 'µ', Ifnull( cod_evnt, '_' )
                            , 'µ', Ifnull( canal_paiement, '_' ), 'µ'
                            , Ifnull( id_statut, '_' ), 'µ' ) crit_agregat
                , CASE
                    WHEN contrepartie = 'D'
                        THEN id_srv_emt
                    ELSE id_srv_dst
                    END id_srv_ref
                , cod_echange
                , cod_ind_cir
                , crit_supl_1
                , crit_supl_2
                , crit_supl_3
                , crit_supl_4
                , crit_supl_5
                , crit_supl_6
                , crit_supl_7
                , crit_supl_8
                , crit_supl_9
        FROM tempsw_fact_agr_tmp1 tmp1
            LEFT OUTER JOIN "FLUX_REF"."REF_BANQ_SWIFT" swfe
                ON ( tmp1.bic_emt = swfe.cod_bic11 )
            LEFT OUTER JOIN "FLUX_REF"."REF_BANQ_SWIFT" swfd
                ON ( tmp1.bic_dst = swfd.cod_bic11 )
            LEFT OUTER JOIN `tempsw_fact_qualifiant` rfactq
                ON ( rfactq.groupe_1 = tmp1.famille_etab_emt
                        AND rfactq.groupe_2 = tmp1.famille_etab_dst ) )
    , step_id_swift_all AS (
        SELECT DISTINCT
            'SWIFT' AS perimetre
            , application
            , id_srv_ref AS id_metier
        FROM tempsw_fact_agr_tmp2 tmp2
        WHERE cod_client_fact = 'INCONNU'
            OR cod_client_fact IS NULL
            OR CASE
                WHEN cod_sens = 'E'
                    THEN famille_etab_emt
                ELSE famille_etab_dst
                END = 'INCONNU' )
    , tempgm_fact_client_emt AS (
        SELECT  id_metier
                , application
        FROM "FLUX_REF"."REF_FACT_CLIENT"
        WHERE ( SELECT date_debut_periode
                FROM date_parm ) BETWEEN dat_deb_client
                                     AND dat_fin_client )
    , tempgm_fact_client_dst AS (
        SELECT  id_metier
                , application
        FROM "FLUX_REF"."REF_FACT_CLIENT"
        WHERE ( SELECT date_debut_periode
                FROM date_parm ) BETWEEN dat_deb_client
                                     AND dat_fin_client )
    , tempgm_rf_toge AS (
        SELECT DISTINCT
            'R' sens
            , Substr( cnrfop, 1, 13 ) idref
            , CASE
                WHEN Trim( COUFGE ) = ''
                    THEN '_'
                ELSE Trim( COUFGE )
                END ufo
            , 'R-OVR' tb_src
        FROM "FLUX_INT"."NRM_TOGE_R_OVR" ovr
        WHERE darcpr BETWEEN (  SELECT date_debut_periode
                                FROM date_parm )
                         AND (  SELECT date_fin_periode
                                FROM date_parm )
            AND coetbl = '000'
        UNION ALL
        SELECT DISTINCT
            'E' sens
            , Substr( cnorcl, 1, 13 ) idref
            , CASE
                WHEN Trim( COD_UFO ) = ''
                    THEN '_'
                ELSE Trim( COD_UFO )
                END ufo
            , 'A-OB' tb_src
        FROM "FLUX_INT"."NRM_TOGE_A_OB" ob
        WHERE dat_per BETWEEN ( SELECT date_debut_periode
                                FROM date_parm )
                             AND (  SELECT date_fin_periode
                                    FROM date_parm ) )
    , tempgm_service AS (
        SELECT  cr_vgm_num_piste num_piste
                , Substr( vgm.cr_vgm_ref_estampillage, 1, 13 ) ref_estamp
                , CASE
                    WHEN cr_vgm_sens_msg = 'R'
                            AND rft1.ufo IS NOT NULL
                        THEN rft1.ufo
                    WHEN cr_vgm_sens_msg = 'E'
                            AND rft2.ufo IS NOT NULL
                        THEN rft2.ufo
                    WHEN cr_vgm_sens_msg = 'R'
                            AND rft2.ufo IS NOT NULL
                        THEN rft2.ufo
                    WHEN cr_vgm_sens_msg = 'E'
                            AND rft1.ufo IS NOT NULL
                        THEN rft1.ufo
                    ELSE NULL
                    END code_service
        FROM "FLUX_CRISTAL"."NRM_CR_VGM" vgm
            LEFT OUTER JOIN tempgm_rf_toge rft1
                ON ( rft1.idref = Substr( vgm.cr_vgm_ref_estampillage, 1, 13 )
                        AND rft1.sens = 'R' )
            LEFT OUTER JOIN tempgm_rf_toge rft2
                ON ( rft2.idref = Substr( vgm.cr_vgm_ref_estampillage, 1, 13 )
                        AND rft2.sens = 'E' )
        WHERE cr_vgm_date_reglt BETWEEN (   SELECT date_debut_periode
                                            FROM date_parm )
                                     AND (  SELECT date_fin_periode
                                            FROM date_parm )
            AND cr_vgm_canal != 'COR' )
    , tempgm_abo AS (
        SELECT  cr_vgm_num_piste abo_num_piste
                , CASE
                    WHEN Ifnull( Trim( CR_VGM_ABO ), '' ) = ''
                        THEN '_'
                    ELSE Trim( CR_VGM_ABO )
                    END abo_orig
                , CASE
                    WHEN ( Trim( Cr_Vgm_Cod_Statut ) = '14'
                                AND cr_vgm_sens_msg = 'E'
                                AND Ifnull( Trim( Cr_Vgm_Abo ), '_' ) NOT IN ( 'NATIXIS', 'BPCE DIN BO', 'CE 30007' ) )
                        THEN 'CRISTAL_EX_NATX'
                    WHEN ( cr_vgm_sens_msg = 'R'
                                AND Ifnull( Trim( Cr_Vgm_Abo ), '_' ) NOT IN ( 'BPCE DIN BO', 'CE 30007' )
                                AND NOT ( Trim( Cr_Vgm_Abo ) = 'NATIXIS'
                                            AND Trim( Cr_Vgm_Bic_Recep ) = 'NATXFRPPXXX' ) )
                        THEN 'CRISTAL_EX_NATX'
                    WHEN ( Trim( Cr_Vgm_Cod_Statut ) = '14'
                                AND cr_vgm_sens_msg = 'E'
                                AND Ifnull( Trim( Cr_Vgm_Abo ), '_' ) = 'NATIXIS' )
                        THEN 'CRISTAL_NATX_E'
                    WHEN ( cr_vgm_sens_msg = 'R'
                                AND Ifnull( Trim( Cr_Vgm_Abo ), '_' ) = 'NATIXIS'
                                AND Trim( Cr_Vgm_Bic_Recep ) = 'NATXFRPPXXX' )
                        THEN 'CRISTAL_NATX_R'
                    ELSE '_'
                    END abo_can_pay
                , Trim( Cr_Vgm_Ref_Estampillage ) abo_ref_estamp
                , cr_vgm_sens_msg abo_sens
                , Ifnull( svc.code_service, '' ) abo_code_service
        FROM "FLUX_CRISTAL"."NRM_CR_VGM"
            INNER JOIN tempgm_service svc
                ON ( svc.num_piste = cr_vgm_num_piste )
        WHERE cr_vgm_date_reglt BETWEEN (   SELECT date_debut_periode
                                            FROM date_parm )
                                     AND (  SELECT date_fin_periode
                                            FROM date_parm )
            AND cr_vgm_canal != 'COR' )
    , tempgm_fact_cristal_1 AS (
        SELECT DISTINCT
            'CRISTAL' application
            , cr_vgm_sens_msg
            , cr_vgm_date_reglt
            , Date_Trunc( cr_vgm_date_reglt, month ) AS deb_periode
            , CASE
                WHEN cr_vgm_sens_msg = 'E'
                    THEN Trim( CR_VGM_ABO )
                WHEN cr_vgm_sens_msg = 'R'
                        AND Substr( cr_vgm_bic_emet, 1, 4 ) IN ( 'CEPA', 'CCBP', 'NATX' )
                    THEN Trim( CR_VGM_BIC_EMET )
                WHEN cr_vgm_sens_msg = 'R'
                        AND Substr( cr_vgm_bic_emet, 1, 4 ) NOT IN ( 'CEPA', 'CCBP', 'NATX' )
                    THEN Trim( substr(CR_VGM_BIC_EMET, 1, 8) )
                END id_srv_emt
            , CASE
                WHEN cr_vgm_sens_msg = 'R'
                    THEN Trim( CR_VGM_ABO )
                WHEN cr_vgm_sens_msg = 'E'
                        AND Substr( cr_vgm_bic_recep, 1, 4 ) IN ( 'CEPA', 'CCBP', 'NATX' )
                    THEN Trim( CR_VGM_BIC_RECEP )
                WHEN cr_vgm_sens_msg = 'E'
                        AND Substr( cr_vgm_bic_recep, 1, 4 ) NOT IN ( 'CEPA', 'CCBP', 'NATX' )
                    THEN Trim( substr(CR_VGM_BIC_RECEP, 1, 8) )
                END id_srv_dst
        FROM "FLUX_CRISTAL"."NRM_CR_VGM"
            INNER JOIN tempgm_abo abo
                ON ( abo_num_piste = cr_vgm_num_piste )
            LEFT OUTER JOIN "FLUX_REF"."REF_CRISTAL_ABO_GROUPEMENT" refabg
                ON ( refabg.cr_code_groupement = 'CANAL'
                        AND refabg.cod_abo = Trim( CR_VGM_CANAL ) )
        WHERE cr_vgm_date_reglt BETWEEN (   SELECT date_debut_periode
                                            FROM date_parm )
                                     AND (  SELECT date_fin_periode
                                            FROM date_parm )
            AND cr_vgm_canal != 'COR'
            AND ( ( Trim( Cr_Vgm_Cod_Statut ) = '14'
                        AND cr_vgm_sens_msg = 'E'
                        AND Trim( Cr_Vgm_Abo ) NOT IN ( 'NATIXIS', 'BPCE DIN BO', 'CE 30007' ) )
                    OR ( cr_vgm_sens_msg = 'R'
                            AND Trim( Cr_Vgm_Abo ) NOT IN ( 'BPCE DIN BO', 'CE 30007' )
                            AND NOT ( Trim( Cr_Vgm_Abo ) = 'NATIXIS'
                                        AND Trim( Cr_Vgm_Bic_Recep ) = 'NATXFRPPXXX' ) )
                    OR ( Trim( Cr_Vgm_Cod_Statut ) = '14'
                            AND cr_vgm_sens_msg = 'E'
                            AND Trim( Cr_Vgm_Abo ) = 'NATIXIS' )
                    OR ( cr_vgm_sens_msg = 'R'
                            AND Trim( Cr_Vgm_Abo ) = 'NATIXIS'
                            AND Trim( Cr_Vgm_Bic_Recep ) = 'NATXFRPPXXX' ) ) )
    , step_id_gm_all AS (
        SELECT DISTINCT
            'GROS_MONTANT' AS perimetre
            , t1.application
            , deb_periode
            , CASE
                WHEN cr_vgm_sens_msg = 'E'
                    THEN t1.id_srv_emt
                ELSE t1.id_srv_dst
                END AS id_metier
        FROM tempgm_fact_cristal_1 t1
            LEFT JOIN `tempgm_fact_client_emt` femt
                ON ( femt.id_metier = t1.id_srv_emt
                        AND femt.application = 'CRISTAL' )
            LEFT JOIN `tempgm_fact_client_dst` fdst
                ON ( fdst.id_metier = t1.id_srv_dst
                        AND fdst.application = 'CRISTAL' )
        WHERE ( cr_vgm_sens_msg = 'E'
                AND femt.id_metier IS NULL )
            OR ( cr_vgm_sens_msg = 'R'
                    AND fdst.id_metier IS NULL ) )
    , dat_parm_periode AS (
        SELECT  Cast( Format_Date( '%Y%m', (    SELECT date_debut_periode
                                                FROM date_parm ) ) AS INT64 ) parm_periode )
    , tempcbk_pays_xenos AS (
        SELECT DISTINCT
            code_xenos
            , code_banque
        FROM "FLUX_REF"."REF_BQUE_XENOS" )
    , tempcbk_fact_client_emt AS (
        SELECT  id_metier
                , application
                , groupe
                , cd_bank
                , cd_bank_aux
                , id_client
                , lib_client
        FROM "FLUX_REF"."REF_FACT_CLIENT"
        WHERE ( SELECT date_debut_periode
                FROM date_parm ) BETWEEN dat_deb_client
                                     AND dat_fin_client
            AND application IN (    'CRISTAL'
                                    , 'TMT'
                                    , 'TOGE'
                                    , 'XENOS' ) )
    , tempcbk_fact_client_dst AS (
        SELECT  id_metier
                , application
                , groupe
                , cd_bank
                , cd_bank_aux
                , id_client
                , lib_client
        FROM "FLUX_REF"."REF_FACT_CLIENT"
        WHERE ( SELECT date_debut_periode
                FROM date_parm ) BETWEEN dat_deb_client
                                     AND dat_fin_client
            AND application IN (    'CRISTAL'
                                    , 'TMT'
                                    , 'TOGE'
                                    , 'XENOS' ) )
    , tempcbk_fact_client_ctrp AS (
        SELECT  id_metier
                , application
                , groupe
                , cd_bank
                , cd_bank_aux
                , id_client
                , lib_client
        FROM "FLUX_REF"."REF_FACT_CLIENT"
        WHERE ( SELECT date_debut_periode
                FROM date_parm ) BETWEEN dat_deb_client
                                     AND dat_fin_client
            AND application = 'CONTREPART' )
    , tempcbk_fact_qualifiant AS (
        SELECT DISTINCT
            qualifiant
            , groupe_1
            , groupe_2
        FROM "FLUX_REF"."REF_FACT_QUALIFIANT"
        WHERE ( SELECT date_debut_periode
                FROM date_parm ) BETWEEN dat_deb_qualifiant
                                     AND dat_fin_qualifiant
            AND top_bank_identiques IS NULL )
    , tempcbk_ref_emet AS (
        SELECT  cr_audvgm_num_piste
                , Max( Trim( CR_AUDVGM_REF_EMET ) ) cr_audvgm_ref_emet
        FROM "FLUX_CRISTAL"."NRM_CR_AUDIT_VIR"
            INNER JOIN date_parm dt_in
                ON ( 1 = 1 )
            INNER JOIN "FLUX_CRISTAL"."NRM_CR_VGM"
                ON ( cr_vgm_date_reglt BETWEEN dt_in.date_debut_periode
                                             AND dt_in.date_fin_periode
                        AND cr_audvgm_num_piste = cr_vgm_num_piste
                        AND cr_vgm_canal = 'COR'
                        AND ( ( Trim( Cr_Vgm_Cod_Statut ) = '14'
                                    AND cr_vgm_sens_msg = 'E' )
                                OR cr_vgm_sens_msg = 'R' ) )
        GROUP BY cr_audvgm_num_piste
        ORDER BY cr_audvgm_num_piste )
    , tempcbk_cristal AS (
        SELECT  cr_vgm_num_piste
                , cr_vgm_date_reglt
                , Trim( CR_VGM_COD_STATUT ) cr_vgm_cod_statut
                , cr_vgm_sens_msg
                , CASE Trim( CR_VGM_SENS_MSG )
                    WHEN 'E'
                        THEN Trim( CR_VGM_BIC_EMET )
                    ELSE Trim( CR_VGM_BIC_RECEP )
                    END bic_chef_file
                , CASE Trim( CR_VGM_SENS_MSG )
                    WHEN 'E'
                        THEN Trim( CR_VGM_BIC_EMET )
                    ELSE Trim( CR_VGM_BIC_RECEP )
                    END bic_ref
                , Trim( CR_VGM_BIC_EMET ) cr_vgm_bic_emet
                , Trim( CR_VGM_BIC_RECEP ) cr_vgm_bic_recep
                , Trim( CR_VGM_TYP_MSG_SWIFT ) cr_vgm_typ_msg_swift
                , Trim( CR_VGM_CANAL ) cr_vgm_canal
                , 'CBK' lib_sys_reglt
                , Trim( Cr_VGM_CANAL_INIT ) cr_vgm_canal_init
                , cr_vgm_sens_dc
                , Trim( CR_VGM_ABO ) cr_vgm_abo
                , Trim( CR_VGM_CODE_SERVICE ) cr_vgm_code_service
                , cr_vgm_montant
                , cr_vgm_provenance
                , cr_vgm_ind_recycl
                , cr_vgm_devise
                , 'CRISTAL_CBK' canal_paiement
                , CASE
                    WHEN Trim( CR_VGM_ABO ) IS NULL
                        THEN Trim( CR_VGM_CODE_SERVICE )
                    WHEN Trim( CR_VGM_CODE_SERVICE ) IS NULL
                        THEN Trim( CR_VGM_ABO )
                    ELSE Concat( Trim( CR_VGM_ABO ), Trim( CR_VGM_CODE_SERVICE ) )
                    END concat_abo_srv
                , Trim( CR_DVGM_REPART_FRAIS ) cr_dvgm_repart_frais
                , CASE
                    WHEN cr_audvgm_ref_emet IS NULL
                        THEN cr_vgm_ref_emet_roc
                    ELSE cr_audvgm_ref_emet
                    END ref_op
                , CASE
                    WHEN cr_vgm_cod_statut IN ( '16', '18', '19' )
                            OR cr_vgm_provenance = 'REP'
                            OR Trim( cr_vgm_abo ) IN ( 'NNCP30007', 'BPCE MIMI' )
                        THEN 'EM'
                    WHEN cr_vgm_ind_recycl = 'M'
                            AND cr_vgm_sens_msg = 'E'
                        THEN 'EM'
                    WHEN cr_vgm_ind_recycl = 'M'
                            AND cr_vgm_sens_msg = 'R'
                        THEN 'RM'
                    WHEN cr_svgm_abo_init = 'REAFF'
                            AND cr_vgm_sens_msg = 'R'
                        THEN 'RM'
                    ELSE CASE
                            WHEN cr_vgm_sens_msg = 'E'
                                THEN 'ESTP'
                            ELSE 'RSTP'
                            END
                    END stp
                , CASE
                    WHEN cr_vgm_sens_msg = 'E'
                        THEN Trim( CR_VGM_ABO )
                    WHEN cr_vgm_sens_msg = 'R'
                            AND Substr( cr_vgm_bic_emet, 1, 4 ) IN ( 'CEPA', 'CCBP', 'NATX' )
                        THEN Trim( CR_VGM_BIC_EMET )
                    WHEN cr_vgm_sens_msg = 'R'
                            AND Substr( cr_vgm_bic_emet, 1, 4 ) NOT IN ( 'CEPA', 'CCBP', 'NATX' )
                        THEN Trim( substr(CR_VGM_BIC_EMET, 1, 8) )
                    END id_srv_emt
                , CASE
                    WHEN cr_vgm_sens_msg = 'R'
                        THEN Trim( CR_VGM_ABO )
                    WHEN cr_vgm_sens_msg = 'E'
                            AND Substr( cr_vgm_bic_recep, 1, 4 ) IN ( 'CEPA', 'CCBP', 'NATX' )
                        THEN Trim( CR_VGM_BIC_RECEP )
                    WHEN cr_vgm_sens_msg = 'E'
                            AND Substr( cr_vgm_bic_recep, 1, 4 ) NOT IN ( 'CEPA', 'CCBP', 'NATX' )
                        THEN Trim( substr(CR_VGM_BIC_RECEP, 1, 8) )
                    END id_srv_dst
                , CASE
                    WHEN cr_vgm_sens_msg = 'E'
                        THEN 'D'
                    ELSE 'E'
                    END contrepartie
                ,  Cast( (  SELECT date_debut_periode
                            FROM date_parm ) AS DATE ) periode
                , Concat(   Ifnull( Trim( CR_VGM_SENS_MSG ), '_' ), 'µ', 'MT'
                            , Ifnull( Trim( CR_VGM_TYP_MSG_SWIFT ), '_' ), 'µ', 'CRISTAL_CBK'
                            , 'µ', Ifnull( Trim( CR_VGM_DEVISE ), '_' ), 'µ'
                            , Ifnull( Trim( CASE WHEN cr_vgm_cod_statut IN ('16', '18', '19') OR cr_vgm_provenance = 'REP' OR Trim( cr_vgm_abo ) IN ('NNCP30007', 'BPCE MIMI') THEN 'EM' WHEN cr_vgm_ind_recycl = 'M' AND CR_VGM_SENS_MSG = 'E' THEN 'EM' WHEN cr_vgm_ind_recycl = 'M' AND CR_VGM_SENS_MSG = 'R' THEN 'RM' WHEN CR_SVGM_ABO_INIT = 'REAFF' AND CR_VGM_SENS_MSG = 'R' THEN 'RM' ELSE CASE WHEN CR_VGM_SENS_MSG = 'E' THEN 'ESTP' ELSE 'RSTP' END END ), '_' ), 'µ', Ifnull( Trim( CR_DVGM_REPART_FRAIS ), '_' )
                            , 'µ', Ifnull( Trim( CR_VGM_BIC_EMET ), '_' ), 'µ'
                            , Ifnull( Trim( substr(CR_VGM_BIC_EMET, 5, 2) ), '_' ), 'µ', Ifnull( Trim( CR_VGM_BIC_RECEP ), '_' )
                            , 'µ', Ifnull( Trim( substr(CR_VGM_BIC_RECEP, 5, 2) ), '_' ), 'µ' ) crit_agregat
        FROM "FLUX_CRISTAL"."NRM_CR_VGM"
            LEFT OUTER JOIN tempcbk_ref_emet
                ON ( cr_vgm_num_piste = cr_audvgm_num_piste )
        WHERE cr_vgm_date_reglt BETWEEN (   SELECT date_debut_periode
                                            FROM date_parm )
                                     AND (  SELECT date_fin_periode
                                            FROM date_parm )
            AND cr_vgm_canal = 'COR'
            AND ( ( Trim( Cr_Vgm_Cod_Statut ) = '14'
                        AND cr_vgm_sens_msg = 'E' )
                    OR cr_vgm_sens_msg = 'R' )
        ORDER BY cr_vgm_num_piste )
    , tempcbk_cristal_tmp_t2 AS (
        SELECT  cr_vgm_num_piste
                , periode
                , 'CBK' filiere
                , 'CRISTAL' application
                , cr_vgm_cod_statut statut
                , bic_ref cod_bic_ref
                , '_' cod_etab_ref
                , bic_chef_file
                , CASE
                    WHEN bic_chef_file = 'BPCEFRPPXXX'
                        THEN '16188'
                    WHEN bic_chef_file = 'NATXFRPPXXX'
                        THEN '30007'
                    ELSE '_'
                    END cd_bank_chef_file
                , '_' cd_bank_cl
                , CASE
                    WHEN cr_vgm_sens_msg = 'E'
                        THEN femt.groupe
                    ELSE fdst.groupe
                    END groupe
                , cr_vgm_sens_msg cod_sens
                , cr_vgm_typ_msg_swift msg_type
                , cr_vgm_canal cod_canal_rgl
                , 'CBK' lib_canal_rgl
                , lib_sys_reglt lib_sys_rgl
                , ref_op ref_op
                , '_' cod_unit
                , '_' lib_unit
                , '_' cod_evnt
                , '_' lib_evnt
                , canal_paiement canal_paiement
                , '_' typ_ind
                , '_' id_statut
                , '_' lib_statut
                , CASE
                    WHEN cr_vgm_sens_msg = 'E'
                        THEN femt.id_client
                    ELSE fdst.id_client
                    END cod_client_fact
                , cr_vgm_devise cd_devise
                , stp cod_typ_op
                , CASE
                    WHEN stp = 'EM'
                        THEN 'Emis Manuel'
                    WHEN stp = 'RM'
                        THEN 'Recu Manuel'
                    WHEN stp = 'ESTP'
                        THEN 'Emis STP'
                    WHEN stp = 'RSTP'
                        THEN 'Recu STP'
                    ELSE '_'
                    END lib_typ_op
                , CASE
                    WHEN Trim( CR_DVGM_REPART_FRAIS ) IS NULL
                        THEN '_'
                    ELSE cr_dvgm_repart_frais
                    END cod_typ_trt_frais
                , cr_vgm_date_reglt date_echange
                , cr_vgm_sens_dc lib_cod_signe_op
                , stp cod_ope
                , '_' ss_cod_ope
                , '_' lib_ss_cod_ope
                , '_' cod_taxable
                , '_' lib_cod_taxable
                , '_' cd_moyen_pymnt
                , '_' lib_moyen_pymnt
                , '_' frais_trsp_dhl
                , '_' cd_pays_frais_trsp_dhl
                , cr_vgm_bic_emet bic_emt
                , Substr( cr_vgm_bic_emet, 5, 2 ) pays_emt
                , id_srv_emt id_srv_emt
                , '_' cod_bank_emt
                , '_' lib_etab_emt
                , CASE
                    WHEN femt.groupe IS NULL
                            AND contrepartie = 'D'
                        THEN 'INCONNU'
                    WHEN femt.groupe IS NOT NULL
                        THEN femt.groupe
                    ELSE fctre.groupe
                    END famille_etab_emt
                , cr_vgm_bic_recep bic_dst
                , Substr( cr_vgm_bic_recep, 5, 2 ) pays_dst
                , id_srv_dst id_srv_dst
                , '_' cod_bank_dst
                , '_' lib_etab_dst
                , CASE
                    WHEN fdst.groupe IS NULL
                            AND contrepartie = 'E'
                        THEN 'INCONNU'
                    WHEN fdst.groupe IS NOT NULL
                        THEN fdst.groupe
                    ELSE fctrd.groupe
                    END famille_etab_dst
                , '_' qualifiant_flux
                , contrepartie
                , '_' tiers_op
                , '_' type_remettant
                , 1 quantite
                , CASE
                    WHEN cr_vgm_devise != 'EUR'
                        THEN cr_vgm_montant
                    ELSE 0
                    END mnt_dev
                , CASE
                    WHEN cr_vgm_devise = 'EUR'
                        THEN cr_vgm_montant
                    ELSE 0
                    END mnt_eur
                , '_' cod_ent_ges_vb
                , '_' cod_eve_ges_vb
                , crit_agregat crit_agregat
                , CASE
                    WHEN cr_vgm_sens_msg = 'E'
                        THEN id_srv_emt
                    ELSE id_srv_dst
                    END id_srv_ref
                , '_' cod_echange
                , '_' cod_ind_cir
                ,  Cast( NULL AS STRING ) crit_supl_1
                ,  Cast( NULL AS STRING ) crit_supl_2
                ,  Cast( NULL AS STRING ) crit_supl_3
                ,  Cast( NULL AS STRING ) crit_supl_4
                ,  Cast( NULL AS STRING ) crit_supl_5
                ,  Cast( NULL AS STRING ) crit_supl_6
                ,  Cast( NULL AS STRING ) crit_supl_7
                ,  Cast( NULL AS STRING ) crit_supl_8
                ,  Cast( NULL AS STRING ) crit_supl_9
        FROM tempcbk_cristal t1
            LEFT JOIN `tempcbk_fact_client_emt` femt
                ON ( femt.id_metier = t1.id_srv_emt
                        AND femt.application = 'CRISTAL' )
            LEFT JOIN `tempcbk_fact_client_dst` fdst
                ON ( fdst.id_metier = t1.id_srv_dst
                        AND fdst.application = 'CRISTAL' )
            LEFT JOIN `tempcbk_fact_client_ctrp` fctre
                ON ( fctre.id_metier = t1.id_srv_emt
                        AND fctre.application = 'CONTREPART' )
            LEFT JOIN `tempcbk_fact_client_ctrp` fctrd
                ON ( fctrd.id_metier = t1.id_srv_dst
                        AND fctrd.application = 'CONTREPART' ) )
    , tempcbk_fact_cristal AS (
        SELECT  periode
                , filiere
                , application
                , cod_bic_ref
                , cod_etab_ref
                , bic_chef_file
                , cd_bank_chef_file
                , cd_bank_cl
                , groupe
                , cod_sens
                , msg_type
                , cod_canal_rgl
                , lib_canal_rgl
                , lib_sys_rgl
                , ref_op
                , cod_unit
                , lib_unit
                , cod_evnt
                , lib_evnt
                , canal_paiement
                , typ_ind
                , id_statut
                , lib_statut
                , cod_client_fact
                , cd_devise
                , cod_typ_op
                , lib_typ_op
                , cod_typ_trt_frais
                , date_echange
                , lib_cod_signe_op
                , cod_ope
                , ss_cod_ope
                , lib_ss_cod_ope
                , cod_taxable
                , lib_cod_taxable
                , cd_moyen_pymnt
                , lib_moyen_pymnt
                , frais_trsp_dhl
                , cd_pays_frais_trsp_dhl
                , bic_emt
                , pays_emt
                , id_srv_emt
                , cod_bank_emt
                , CASE
                    WHEN Trim( RBSE.LIB_INST_NAME ) IS NOT NULL
                        THEN Trim( RBSE.LIB_INST_NAME )
                    ELSE '_'
                    END lib_etab_emt
                , famille_etab_emt
                , bic_dst
                , pays_dst
                , id_srv_dst
                , cod_bank_dst
                , CASE
                    WHEN Trim( RBSD.LIB_INST_NAME ) IS NOT NULL
                        THEN Trim( RBSD.LIB_INST_NAME )
                    ELSE '_'
                    END lib_etab_dst
                , famille_etab_dst
                , CASE
                    WHEN famille_etab_emt = 'TECHNIQUE'
                            OR famille_etab_dst = 'TECHNIQUE'
                        THEN 'TECHNIQUE'
                    WHEN Ifnull( famille_etab_emt, '_' ) = '_'
                            AND contrepartie = 'D'
                        THEN 'INCONNU'
                    WHEN Ifnull( famille_etab_dst, '_' ) = '_'
                            AND contrepartie = 'E'
                        THEN 'INCONNU'
                    WHEN Ifnull( famille_etab_emt, '_' ) = '_'
                            OR Ifnull( famille_etab_dst, '_' ) = '_'
                        THEN 'INTERBANCAIRE'
                    ELSE rfactq.qualifiant
                    END qualifiant_flux
                , contrepartie
                , tiers_op
                , type_remettant
                , cod_ent_ges_vb
                , cod_eve_ges_vb
                , cod_echange
                , cod_ind_cir
                , crit_agregat
                ,  Cast( NULL AS STRING ) crit_supl_1
                ,  Cast( NULL AS STRING ) crit_supl_2
                ,  Cast( NULL AS STRING ) crit_supl_3
                ,  Cast( NULL AS STRING ) crit_supl_4
                ,  Cast( NULL AS STRING ) crit_supl_5
                ,  Cast( NULL AS STRING ) crit_supl_6
                ,  Cast( NULL AS STRING ) crit_supl_7
                ,  Cast( NULL AS STRING ) crit_supl_8
                ,  Cast( NULL AS STRING ) crit_supl_9
                , id_srv_ref
                , "" file_gen
                , CURRENT_TIMESTAMP creation_dt
                ,
                 Cast( Format_Date( '%G', periode ) AS INT ) YEAR
                ,
                 Cast( Format_Date( '%m', periode ) AS INT ) MONTH
                , quantite
                , Round( mnt_dev, 2 ) mnt_dev
                , mnt_eur
        FROM tempcbk_cristal_tmp_t2 t2
            LEFT OUTER JOIN `tempcbk_fact_qualifiant` rfactq
                ON ( rfactq.groupe_1 = t2.famille_etab_emt
                        AND rfactq.groupe_2 = t2.famille_etab_dst )
            LEFT OUTER JOIN "FLUX_REF"."REF_BANQ_SWIFT" rbse
                ON ( rbse.cod_bic11 = t2.bic_emt )
            LEFT OUTER JOIN "FLUX_REF"."REF_BANQ_SWIFT" rbsd
                ON ( rbsd.cod_bic11 = t2.bic_dst ) )
    , tempcbk_tmt AS (
        SELECT  tmt_b.periode periode
                , 'CBK' filiere
                , 'TMT' application
                , 'BPCEFRPPXXX' bic_chef_file
                , NULL cd_bank_chef_file
                , tmt_b.cod_bic_do cod_bic_ref
                , 'ETMT' cod_typ_op
                , 'ETMT' cod_ope
                , 'Emis TMT' lib_typ_op
                , '103' msg_type
                , tmt_b.cod_dev_32b cd_devise
                , Ifnull( Trim( tmt_a.COD_DET_CHA ), '_' ) cod_typ_trt_frais
                , 'TMT' canal_paiement
                , tmt_b.dat_cre_piv date_echange
                , Substr( Concat( tmt_b.cod_ref_mt103, '_' ), 1, 16 ) ref_op
                , 'E' cod_sens
                , tmt_b.cod_bic_do id_srv_emt
                , tmt_b.cod_bic_do bic_emt
                , Ifnull( Substr( tmt_b.cod_bic_ban_ten_ben, 1, 8 ), '8' ) id_srv_dst
                , tmt_b.cod_bic_ban_ten_ben bic_dst
                , 'D' contrepartie
                , '_' tiers_op
                , '_' type_remettant
                , 1 quantite
                , tmt_b.mnt_32b mnt_dev
                , tmt_b.mnt_32b mnt_eur
        FROM flux_int.nrm_tmt_b tmt_b
            JOIN flux_int.nrm_tmt_a tmt_a
                ON tmt_a.cod_ref_mt102 = tmt_b.cod_ref_mt102
        WHERE tmt_b.periode = ( SELECT parm_periode
                                FROM dat_parm_periode )
        UNION ALL
        SELECT  tmt_a.periode periode
                , 'CBK' filiere
                , 'TMT' application
                , tmt_a.cod_bic_eme bic_chef_file
                , NULL cd_bank_chef_file
                , tmt_a.cod_bic_eme cod_bic_ref
                , 'ETMT' cod_typ_op
                , 'ETMT' cod_ope
                , 'Emis TMT' lib_typ_op
                , '102' msg_type
                , NULL cd_devise
                , Ifnull( Trim( tmt_a.COD_DET_CHA ), '_' ) cod_typ_trt_frais
                , 'TMT' canal_paiement
                , tmt_a.dat_cre_piv date_echange
                , tmt_a.cod_ref_mt102 ref_op
                , 'E' cod_sens
                , tmt_a.cod_bic_eme id_srv_emt
                , tmt_a.cod_bic_eme bic_emt
                , Ifnull( Substr( tmt_a.cod_bic_dest, 1, 8 ), '_' ) id_srv_dst
                , tmt_a.cod_bic_dest bic_dst
                , 'D' contrepartie
                , 'S/O' tiers_op
                , 'S/O' type_remettant
                , 1 quantite
                , 0 mnt_dev
                , 0 mnt_eur
        FROM flux_int.nrm_tmt_a tmt_a
        WHERE tmt_a.periode = ( SELECT parm_periode
                                FROM dat_parm_periode ) )
    , tempcbk_fact_tmt_t1 AS (
        SELECT   Cast( Concat(  Substr(  Cast( periode AS STRING ), 1, 4 )
                                , '-'
                                , Substr(  Cast( periode AS STRING ), 5, 2 )
                                , '-01' ) AS DATE ) periode
                , 'CBK' filiere
                , 'TMT' application
                , '_' statut
                , cod_bic_ref cod_bic_ref
                , '_' cod_etab_ref
                , bic_chef_file
                , CASE
                    WHEN bic_chef_file = 'BPCEFRPPXXX'
                        THEN '16188'
                    WHEN bic_chef_file = 'NATXFRPPXXX'
                        THEN '30007'
                    ELSE '_'
                    END cd_bank_chef_file
                , '_' cd_bank_cl
                , CASE
                    WHEN femt.groupe IS NOT NULL
                        THEN femt.groupe
                    ELSE '_'
                    END groupe
                , cod_sens
                , msg_type
                , '_' cod_canal_rgl
                , '_' lib_canal_rgl
                , '_' lib_sys_rgl
                , ref_op
                , '_' cod_unit
                , '_' lib_unit
                , '_' cod_evnt
                , '_' lib_evnt
                , canal_paiement
                , '_' typ_ind
                , '_' id_statut
                , '_' lib_statut
                , CASE
                    WHEN femt.id_client IS NOT NULL
                        THEN femt.id_client
                    ELSE '_'
                    END cod_client_fact
                , CASE
                    WHEN cd_devise IS NULL
                        THEN '_'
                    ELSE cd_devise
                    END cd_devise
                , cod_typ_op
                , lib_typ_op
                , cod_typ_trt_frais
                , date_echange
                , '_' lib_cod_signe_op
                , cod_ope
                , '_' ss_cod_ope
                , '_' lib_ss_cod_ope
                , '_' cod_taxable
                , '_' lib_cod_taxable
                , '_' cd_moyen_pymnt
                , '_' lib_moyen_pymnt
                , '_' frais_trsp_dhl
                , '_' cd_pays_frais_trsp_dhl
                , bic_emt
                , Substr( bic_emt, 5, 2 ) pays_emt
                , id_srv_emt
                , '_' cod_bank_emt
                , '_' lib_etab_emt
                , CASE
                    WHEN femt.groupe IS NULL
                            AND contrepartie = 'D'
                        THEN 'INCONNU'
                    WHEN femt.groupe IS NOT NULL
                        THEN femt.groupe
                    ELSE fctre.groupe
                    END famille_etab_emt
                , bic_dst
                , Substr( bic_dst, 5, 2 ) pays_dst
                , id_srv_dst
                , '_' cod_bank_dst
                , '_' lib_etab_dst
                , CASE
                    WHEN fdst.groupe IS NULL
                            AND contrepartie = 'E'
                        THEN 'INCONNU'
                    WHEN fdst.groupe IS NOT NULL
                        THEN fdst.groupe
                    ELSE fctrd.groupe
                    END famille_etab_dst
                , '_' qualifiant_flux
                , 'D' contrepartie
                , 'S/O' tiers_op
                , 'S/O' type_remettant
                , quantite
                , mnt_dev
                , mnt_eur
                , '_' cod_ent_ges_vb
                , '_' cod_eve_ges_vb
                , Concat(   cod_sens, 'µ', msg_type
                            , 'µ', canal_paiement, 'µ'
                            , cd_devise, 'µ', cod_typ_op
                            , 'µ', cod_typ_trt_frais, 'µ'
                            , bic_emt, 'µ', Substr( bic_emt, 5, 2 )
                            , 'µ', bic_dst, 'µ'
                            , Substr( bic_dst, 5, 2 ), 'µ' ) crit_agregat
                , CASE
                    WHEN cod_sens = 'E'
                        THEN id_srv_emt
                    ELSE id_srv_dst
                    END id_srv_ref
                , '_' cod_echange
                , '_' cod_ind_cir
                ,  Cast( NULL AS STRING ) crit_supl_1
                ,  Cast( NULL AS STRING ) crit_supl_2
                ,  Cast( NULL AS STRING ) crit_supl_3
                ,  Cast( NULL AS STRING ) crit_supl_4
                ,  Cast( NULL AS STRING ) crit_supl_5
                ,  Cast( NULL AS STRING ) crit_supl_6
                ,  Cast( NULL AS STRING ) crit_supl_7
                ,  Cast( NULL AS STRING ) crit_supl_8
                ,  Cast( NULL AS STRING ) crit_supl_9
        FROM tempcbk_tmt t1
            LEFT JOIN `tempcbk_fact_client_emt` femt
                ON ( femt.id_metier = t1.id_srv_emt
                        AND femt.application = 'TMT' )
            LEFT JOIN `tempcbk_fact_client_dst` fdst
                ON ( fdst.id_metier = t1.id_srv_dst
                        AND fdst.application = 'TMT' )
            LEFT JOIN `tempcbk_fact_client_ctrp` fctre
                ON ( fctre.id_metier = t1.id_srv_emt
                        AND fctre.application = 'CONTREPART' )
            LEFT JOIN `tempcbk_fact_client_ctrp` fctrd
                ON ( fctrd.id_metier = t1.id_srv_dst
                        AND fctrd.application = 'CONTREPART' ) )
    , tempcbk_fact_tmt AS (
        SELECT  periode
                , filiere
                , application
                , cod_bic_ref
                , cod_etab_ref
                , bic_chef_file
                , cd_bank_chef_file
                , cd_bank_cl
                , groupe
                , cod_sens
                , msg_type
                , cod_canal_rgl
                , lib_canal_rgl
                , lib_sys_rgl
                , ref_op
                , cod_unit
                , lib_unit
                , cod_evnt
                , lib_evnt
                , canal_paiement
                , typ_ind
                , id_statut
                , lib_statut
                , cod_client_fact
                , cd_devise
                , cod_typ_op
                , lib_typ_op
                , cod_typ_trt_frais
                , date_echange
                , lib_cod_signe_op
                , cod_ope
                , ss_cod_ope
                , lib_ss_cod_ope
                , cod_taxable
                , lib_cod_taxable
                , cd_moyen_pymnt
                , lib_moyen_pymnt
                , frais_trsp_dhl
                , cd_pays_frais_trsp_dhl
                , bic_emt
                , pays_emt
                , id_srv_emt
                , cod_bank_emt
                , CASE
                    WHEN Trim( RBSE.LIB_INST_NAME ) IS NOT NULL
                        THEN Trim( RBSE.LIB_INST_NAME )
                    ELSE '_'
                    END lib_etab_emt
                , famille_etab_emt
                , bic_dst
                , pays_dst
                , id_srv_dst
                , cod_bank_dst
                , CASE
                    WHEN Trim( RBSD.LIB_INST_NAME ) IS NOT NULL
                        THEN Trim( RBSD.LIB_INST_NAME )
                    ELSE '_'
                    END lib_etab_dst
                , famille_etab_dst
                , CASE
                    WHEN famille_etab_emt = 'TECHNIQUE'
                            OR famille_etab_dst = 'TECHNIQUE'
                        THEN 'TECHNIQUE'
                    WHEN Ifnull( famille_etab_emt, '_' ) = '_'
                            AND contrepartie = 'D'
                        THEN 'INCONNU'
                    WHEN Ifnull( famille_etab_dst, '_' ) = '_'
                            AND contrepartie = 'E'
                        THEN 'INCONNU'
                    WHEN Ifnull( famille_etab_emt, '_' ) = '_'
                            OR Ifnull( famille_etab_dst, '_' ) = '_'
                        THEN 'INTERBANCAIRE'
                    ELSE rfactq.qualifiant
                    END qualifiant_flux
                , contrepartie
                , tiers_op
                , type_remettant
                , cod_ent_ges_vb
                , cod_eve_ges_vb
                , cod_echange
                , cod_ind_cir
                , crit_agregat
                , crit_supl_1
                , crit_supl_2
                , crit_supl_3
                , crit_supl_4
                , crit_supl_5
                , crit_supl_6
                , crit_supl_7
                , crit_supl_8
                , crit_supl_9
                , id_srv_ref
                , "" file_gen
                , CURRENT_TIMESTAMP creation_dt
                ,
                 Cast( Format_Date( '%G', periode ) AS INT ) YEAR
                ,
                 Cast( Format_Date( '%m', periode ) AS INT ) MONTH
                , quantite
                , mnt_dev
                , mnt_eur
        FROM tempcbk_fact_tmt_t1 t1
            LEFT OUTER JOIN `tempcbk_fact_qualifiant` rfactq
                ON ( rfactq.groupe_1 = t1.famille_etab_emt
                        AND rfactq.groupe_2 = t1.famille_etab_dst )
            LEFT OUTER JOIN "FLUX_REF"."REF_BANQ_SWIFT" rbse
                ON ( rbse.cod_bic11 = t1.bic_emt )
            LEFT OUTER JOIN "FLUX_REF"."REF_BANQ_SWIFT" rbsd
                ON ( rbsd.cod_bic11 = t1.bic_dst ) )
    , tempcbk_codcat AS (
        SELECT DISTINCT
            cod_etab
        FROM "FLUX_REF"."REF_FIB_FGD"
        WHERE cod_cat IN ( 'SPCE', 'SPBP' )
        ORDER BY cod_etab )
    , tempcbk_togeaob AS (
        SELECT   Cast( toge_ob.periode AS STRING ) periode
                , toge_ob.top_chg
                , toge_ob.etb_ges
                , toge_ob.top_stp
                , toge_ob.typ_mes
                , toge_ob.codemo
                , toge_ob.cod_cat_frais
                , toge_ob.dat_per
                , toge_ob.cnorcl
                , toge_ob.bic_bef
                , toge_ob.biceme
                , toge_ob.mtord0
                , toge_ob.mnt_eur
                , toge_ob.bic_do_int
                , Trim( toge_Ob.cod_ufo ) cod_ufo
                , ref_etb.cod_bic AS etb_bic
                , ref_etb.cod_bqe AS etb_bqe
                , 'NATXFRPPXXX' bic_chef_file
                , '30007' cd_bank_chef_file
                , CASE
                    WHEN toge_ob.top_stp = 'O'
                        THEN 'ESTP'
                    ELSE 'EM'
                    END cod_typ_op
                , CASE
                    WHEN toge_ob.cod_ufo IS NULL
                            OR Trim( toge_Ob.COD_UFO ) = ''
                        THEN Concat( toge_ob.etb_ges, '___' )
                    ELSE Concat( toge_ob.etb_ges, toge_ob.cod_ufo )
                    END id_srv_emt
                , Ifnull( Substr( Trim( toge_Ob.BIC_BEF ), 1, 8 ), '_' ) id_srv_dst
                , toge_ob.bic_bef bic_dst
                , 'E' cod_sens
        FROM "FLUX_INT"."NRM_TOGE_A_OB" toge_ob
            JOIN "FLUX_REF"."REF_TOGE_DIVERS" ref_div
                ON ref_div.cod_idx = 'S_ORD'
                    AND ref_div.cod_ref = toge_ob.cestvi
                    AND ref_div.flag_statut = 'OK'
            JOIN "FLUX_REF"."REF_TOGE_ETB_GES" ref_etb
                ON ref_etb.cod_etb = toge_ob.etb_ges
            LEFT OUTER JOIN tempcbk_codcat
                ON ( tempcbk_codcat.cod_etab = ref_etb.cod_bqe )
        WHERE toge_ob.periode = (   SELECT parm_periode
                                    FROM dat_parm_periode )
            AND toge_ob.can_pai = 'CBK'
            AND toge_ob.cod_trig NOT IN ( 'TRS', 'OIN' ) )
    , tempcbk_togerovr AS (
        SELECT   Cast( toge_ovr.periode AS STRING ) periode
                , toge_ovr.top_chg
                , toge_ovr.etb_ges
                , toge_ovr.top_stp
                , toge_ovr.typ_mes
                , toge_ovr.ct32a2
                , toge_ovr.ct071s
                , toge_ovr.dat_per
                , toge_ovr.cnrfop
                , toge_ovr.mtnreg
                , toge_ovr.mtdbve
                , toge_ovr.bic_eme
                , toge_ovr.cod_etb_bef
                , toge_ovr.coetbl
                , toge_ovr.coufge
                , ref_etb.cod_bic AS etb_bic
                , ref_etb.cod_bqe AS etb_bqe
                , 'NATXFRPPXXX' bic_chef_file
                , '30007' cd_bank_chef_file
                , CASE
                    WHEN toge_ovr.top_stp = 'O'
                        THEN 'RSTP'
                    ELSE 'RM'
                    END cod_typ_op
                , CASE
                    WHEN Substr( toge_ovr.bic_eme, 1, 4 ) IN ( 'CEPA', 'CCBP', 'NATX' )
                        THEN toge_ovr.bic_eme
                    ELSE Substr( toge_ovr.bic_eme, 1, 8 )
                    END id_srv_emt
                , CASE
                    WHEN toge_ovr.coufge IS NULL
                            OR Trim( Toge_ovr.COUFGE ) = ''
                        THEN Concat( Trim( ETB_GES ), '___' )
                    ELSE Concat( Trim( Toge_ovr.ETB_GES ), Trim( Toge_ovr.COUFGE ) )
                    END id_srv_dst
                , CASE
                    WHEN ( toge_ovr.cod_etb_bef = '_'
                                OR toge_ovr.cod_etb_bef IS NULL )
                        THEN ref_etb.cod_bic
                    WHEN fib.cod_etab IS NULL
                        THEN ref_etb.cod_bic
                    ELSE fib.cod_bic
                    END bic_dst
                , 'R' cod_sens
        FROM "FLUX_INT"."NRM_TOGE_R_OVR" toge_ovr
            JOIN "FLUX_REF"."REF_TOGE_DIVERS" ref_div
                ON ref_div.cod_idx = 'S_ORD'
                    AND ref_div.cod_ref = toge_ovr.ceordv
                    AND ref_div.flag_statut = 'OK'
            JOIN "FLUX_REF"."REF_TOGE_ETB_GES" ref_etb
                ON ref_etb.cod_etb = toge_ovr.etb_ges
            LEFT OUTER JOIN tempcbk_codcat
                ON ( tempcbk_codcat.cod_etab = ref_etb.cod_bqe )
            LEFT OUTER JOIN "FLUX_REF"."REF_FIB_FGD" fib
                ON ( fib.cod_etab = toge_ovr.cod_etb_bef )
        WHERE toge_ovr.periode = (  SELECT parm_periode
                                    FROM dat_parm_periode )
            AND toge_ovr.can_rgl = 'CBK'
            AND toge_ovr.cod_trig = 'ALT'
            AND Ifnull( toge_ovr.coracb, '_' ) != '0440312' )
    , tempcbk_fact_toge_t1 AS (
        SELECT   Cast( Concat(  Substr( periode, 1, 4 )
                                , '-'
                                , Substr( periode, 5, 2 )
                                , '-01' ) AS DATE ) periode
                , 'CBK' filiere
                , 'TOGE' application
                , '_' statut
                , etb_bic cod_bic_ref
                , '_' cod_etab_ref
                , bic_chef_file
                , cd_bank_chef_file
                , '_' cd_bank_cl
                , '_' groupe
                , cod_sens
                , Substr( typ_mes, 3, 3 ) msg_type
                , '_' cod_canal_rgl
                , '_' lib_canal_rgl
                , '_' lib_sys_rgl
                , Ifnull( cnorcl, '_' ) ref_op
                , '_' cod_unit
                , '_' lib_unit
                , '_' cod_evnt
                , '_' lib_evnt
                , 'TOGE_A' canal_paiement
                , '_' typ_ind
                , '_' id_statut
                , '_' lib_statut
                , '_' cod_client_fact
                , codemo cd_devise
                , cod_typ_op
                , '_' lib_typ_op
                , cod_cat_frais cod_typ_trt_frais
                , dat_per date_echange
                , '_' lib_cod_signe_op
                , cod_typ_op cod_ope
                , '_' ss_cod_ope
                , '_' lib_ss_cod_ope
                , '_' cod_taxable
                , '_' lib_cod_taxable
                , '_' cd_moyen_pymnt
                , '_' lib_moyen_pymnt
                , '_' frais_trsp_dhl
                , '_' cd_pays_frais_trsp_dhl
                , CASE
                    WHEN bic_do_int IS NULL
                            OR Trim( BIC_DO_INT ) = ''
                        THEN biceme
                    ELSE bic_do_int
                    END bic_emt
                , CASE
                    WHEN bic_do_int IS NULL
                            OR Trim( BIC_DO_INT ) = ''
                        THEN Substr( biceme, 5, 2 )
                    ELSE Substr( bic_do_int, 5, 2 )
                    END pays_emt
                , id_srv_emt
                , '_' cod_bank_emt
                , '_' lib_etab_emt
                , '_' famille_etab_emt
                , CASE
                    WHEN Length( bic_dst ) = 8
                        THEN Concat( bic_dst, 'XXX' )
                    ELSE bic_dst
                    END bic_dst
                , Substr( bic_dst, 5, 2 ) pays_dst
                , id_srv_dst
                , '_' cod_bank_dst
                , '_' lib_etab_dst
                , '_' famille_etab_dst
                , '_' qualifiant_flux
                , 'D' contrepartie
                , '_' tiers_op
                , '_' type_remettant
                , 1 quantite
                , Round( mtord0, 2 ) mnt_dev
                , Round( mnt_eur, 2 ) mnt_eur
                , '_' cod_ent_ges_vb
                , '_' cod_eve_ges_vb
                , '_' crit_agregat
                , CASE
                    WHEN cod_sens = 'E'
                        THEN id_srv_emt
                    ELSE id_srv_dst
                    END id_srv_ref
                , '_' cod_echange
                , '_' cod_ind_cir
                ,  Cast( NULL AS STRING ) crit_supl_1
                ,  Cast( NULL AS STRING ) crit_supl_2
                ,  Cast( NULL AS STRING ) crit_supl_3
                ,  Cast( NULL AS STRING ) crit_supl_4
                ,  Cast( NULL AS STRING ) crit_supl_5
                ,  Cast( NULL AS STRING ) crit_supl_6
                ,  Cast( NULL AS STRING ) crit_supl_7
                ,  Cast( NULL AS STRING ) crit_supl_8
                ,  Cast( NULL AS STRING ) crit_supl_9
        FROM tempcbk_togeaob
        UNION ALL
        SELECT   Cast( Concat(  Substr( periode, 1, 4 )
                                , '-'
                                , Substr( periode, 5, 2 )
                                , '-01' ) AS DATE ) periode
                , 'CBK' filiere
                , 'TOGE' application
                , '_' statut
                , etb_bic cod_bic_ref
                , '_' cod_etab_ref
                , bic_chef_file
                , '_' cd_bank_chef_file
                , '_' cd_bank_cl
                , '_' groupe
                , cod_sens
                , Substr( typ_mes, 3, 3 ) msg_type
                , '_' cod_canal_rgl
                , '_' lib_canal_rgl
                , '_' lib_sys_rgl
                , Ifnull( cnrfop, '_' ) ref_op
                , '_' cod_unit
                , '_' lib_unit
                , '_' cod_evnt
                , '_' lib_evnt
                , 'TOGE_R' canal_paiement
                , '_' typ_ind
                , '_' id_statut
                , '_' lib_statut
                , '_' cod_client_fact
                , ct32a2 cd_devise
                , cod_typ_op
                , '_' lib_typ_op
                , ct071s cod_typ_trt_frais
                , dat_per date_echange
                , '_' lib_cod_signe_op
                , cod_typ_op cod_ope
                , '_' ss_cod_ope
                , '_' lib_ss_cod_ope
                , '_' cod_taxable
                , '_' lib_cod_taxable
                , '_' cd_moyen_pymnt
                , '_' lib_moyen_pymnt
                , '_' frais_trsp_dhl
                , '_' cd_pays_frais_trsp_dhl
                , bic_eme bic_emt
                , Substr( bic_eme, 5, 2 ) pays_emt
                , Ifnull( id_srv_emt, '_' ) id_srv_emt
                , '_' cod_bank_emt
                , '_' lib_etab_emt
                , '_' famille_etab_emt
                , CASE
                    WHEN Length( bic_dst ) = 8
                        THEN Concat( bic_dst, 'XXX' )
                    ELSE bic_dst
                    END bic_dst
                , Substr( bic_dst, 5, 2 ) pays_dst
                , Ifnull( id_srv_dst, '_' ) id_srv_dst
                , '_' cod_bank_dst
                , '_' lib_etab_dst
                , '_' famille_etab_dst
                , '_' qualifiant_flux
                , 'E' contrepartie
                , '_' tiers_op
                , '_' type_remettant
                , 1 quantite
                , Round( mtnreg, 2 ) mnt_dev
                , Round( mtdbve, 2 ) mnt_eur
                , '_' cod_ent_ges_vb
                , '_' cod_eve_ges_vb
                , '_' crit_agregat
                , CASE
                    WHEN cod_sens = 'E'
                        THEN Ifnull( id_srv_emt, '_' )
                    ELSE Ifnull( id_srv_dst, '_' )
                    END id_srv_ref
                , '_' cod_echange
                , '_' cod_ind_cir
                ,  Cast( NULL AS STRING ) crit_supl_1
                ,  Cast( NULL AS STRING ) crit_supl_2
                ,  Cast( NULL AS STRING ) crit_supl_3
                ,  Cast( NULL AS STRING ) crit_supl_4
                ,  Cast( NULL AS STRING ) crit_supl_5
                ,  Cast( NULL AS STRING ) crit_supl_6
                ,  Cast( NULL AS STRING ) crit_supl_7
                ,  Cast( NULL AS STRING ) crit_supl_8
                ,  Cast( NULL AS STRING ) crit_supl_9
        FROM tempcbk_togerovr )
    , tempcbk_fact_toge_t2 AS (
        SELECT  periode
                , t1.filiere
                , t1.application
                , cod_bic_ref
                , cod_etab_ref
                , bic_chef_file
                , CASE
                    WHEN bic_chef_file = 'BPCEFRPPXXX'
                        THEN '16188'
                    WHEN bic_chef_file = 'NATXFRPPXXX'
                        THEN '30007'
                    ELSE '_'
                    END cd_bank_chef_file
                , cd_bank_cl
                , CASE
                    WHEN cod_sens = 'E'
                        THEN CASE
                                WHEN femt.groupe IS NOT NULL
                                    THEN femt.groupe
                                ELSE '_'
                                END
                    ELSE CASE
                            WHEN fdst.groupe IS NOT NULL
                                THEN fdst.groupe
                            ELSE '_'
                            END
                    END groupe
                , cod_sens
                , msg_type
                , cod_canal_rgl
                , lib_canal_rgl
                , lib_sys_rgl
                , ref_op
                , cod_unit
                , lib_unit
                , cod_evnt
                , lib_evnt
                , canal_paiement
                , typ_ind
                , id_statut
                , lib_statut
                , CASE
                    WHEN cod_sens = 'E'
                        THEN CASE
                                WHEN femt.id_client IS NOT NULL
                                    THEN femt.id_client
                                ELSE '_'
                                END
                    ELSE CASE
                            WHEN fdst.id_client IS NOT NULL
                                THEN fdst.id_client
                            ELSE '_'
                            END
                    END cod_client_fact
                , cd_devise
                , cod_typ_op
                , CASE
                    WHEN cod_typ_op = 'EM'
                        THEN 'Emis Manuel'
                    WHEN cod_typ_op = 'RM'
                        THEN 'Recu Manuel'
                    WHEN cod_typ_op = 'ESTP'
                        THEN 'Emis STP'
                    WHEN cod_typ_op = 'RSTP'
                        THEN 'Recu STP'
                    ELSE '_'
                    END lib_typ_op
                , cod_typ_trt_frais
                , date_echange
                , lib_cod_signe_op
                , cod_ope
                , ss_cod_ope
                , lib_ss_cod_ope
                , cod_taxable
                , lib_cod_taxable
                , cd_moyen_pymnt
                , lib_moyen_pymnt
                , frais_trsp_dhl
                , cd_pays_frais_trsp_dhl
                , bic_emt
                , pays_emt
                , id_srv_emt
                , cod_bank_emt
                , CASE
                    WHEN Trim( RBSE.LIB_INST_NAME ) IS NOT NULL
                        THEN Trim( RBSE.LIB_INST_NAME )
                    ELSE '_'
                    END lib_etab_emt
                , CASE
                    WHEN femt.groupe IS NULL
                            AND contrepartie = 'D'
                        THEN 'INCONNU'
                    WHEN femt.groupe IS NOT NULL
                        THEN femt.groupe
                    ELSE fctre.groupe
                    END famille_etab_emt
                , bic_dst
                , pays_dst
                , id_srv_dst
                , cod_bank_dst
                , CASE
                    WHEN Trim( RBSD.LIB_INST_NAME ) IS NOT NULL
                        THEN Trim( RBSD.LIB_INST_NAME )
                    ELSE '_'
                    END lib_etab_dst
                , CASE
                    WHEN fdst.groupe IS NULL
                            AND contrepartie = 'E'
                        THEN 'INCONNU'
                    WHEN fdst.groupe IS NOT NULL
                        THEN fdst.groupe
                    ELSE fctrd.groupe
                    END famille_etab_dst
                , qualifiant_flux
                , contrepartie
                , tiers_op
                , type_remettant
                , cod_ent_ges_vb
                , cod_eve_ges_vb
                , cod_echange
                , cod_ind_cir
                , crit_agregat
                , crit_supl_1
                , crit_supl_2
                , crit_supl_3
                , crit_supl_4
                , crit_supl_5
                , crit_supl_6
                , crit_supl_7
                , crit_supl_8
                , crit_supl_9
                , id_srv_ref
                , "" file_gen
                , CURRENT_TIMESTAMP creation_dt
                ,
                 Cast( Format_Date( '%G', periode ) AS INT64 ) YEAR
                ,
                 Cast( Format_Date( '%m', periode ) AS INT64 ) MONTH
                , quantite
                , mnt_dev
                , mnt_eur
                , CASE
                    WHEN femt.cd_bank = fdst.cd_bank
                        THEN 'X'
                    WHEN cod_sens = 'E'
                            AND femt.cd_bank = fctrd.cd_bank
                        THEN 'X'
                    WHEN cod_sens = 'R'
                            AND fdst.cd_bank = fctre.cd_bank
                        THEN 'X'
                    ELSE NULL
                    END top_bq_identique
        FROM tempcbk_fact_toge_t1 t1
            LEFT JOIN `tempcbk_fact_client_emt` femt
                ON ( femt.id_metier = t1.id_srv_emt
                        AND femt.application = 'TOGE' )
            LEFT JOIN `tempcbk_fact_client_dst` fdst
                ON ( fdst.id_metier = t1.id_srv_dst
                        AND fdst.application = 'TOGE' )
            LEFT JOIN `tempcbk_fact_client_ctrp` fctre
                ON ( fctre.id_metier = t1.id_srv_emt
                        AND fctre.application = 'CONTREPART' )
            LEFT JOIN `tempcbk_fact_client_ctrp` fctrd
                ON ( fctrd.id_metier = t1.id_srv_dst
                        AND fctrd.application = 'CONTREPART' )
            LEFT OUTER JOIN "FLUX_REF"."REF_BANQ_SWIFT" rbse
                ON ( rbse.cod_bic11 = t1.bic_emt )
            LEFT OUTER JOIN "FLUX_REF"."REF_BANQ_SWIFT" rbsd
                ON ( rbsd.cod_bic11 = t1.bic_dst ) )
    , tempcbk_fact_toge AS (
        SELECT  periode
                , filiere
                , application
                , cod_bic_ref
                , cod_etab_ref
                , bic_chef_file
                , cd_bank_chef_file
                , cd_bank_cl
                , groupe
                , cod_sens
                , msg_type
                , cod_canal_rgl
                , lib_canal_rgl
                , lib_sys_rgl
                , ref_op
                , cod_unit
                , lib_unit
                , cod_evnt
                , lib_evnt
                , canal_paiement
                , typ_ind
                , id_statut
                , lib_statut
                , cod_client_fact
                , cd_devise
                , cod_typ_op
                , lib_typ_op
                , cod_typ_trt_frais
                , date_echange
                , lib_cod_signe_op
                , cod_ope
                , ss_cod_ope
                , lib_ss_cod_ope
                , cod_taxable
                , lib_cod_taxable
                , cd_moyen_pymnt
                , lib_moyen_pymnt
                , frais_trsp_dhl
                , cd_pays_frais_trsp_dhl
                , Ifnull( bic_emt, '_' ) bic_emt
                , Ifnull( pays_emt, '_' ) pays_emt
                , Ifnull( id_srv_emt, '_' ) id_srv_emt
                , cod_bank_emt
                , lib_etab_emt
                , famille_etab_emt
                , Ifnull( bic_dst, '_' ) bic_dst
                , Ifnull( pays_dst, '_' ) pays_dst
                , Ifnull( id_srv_dst, '_' ) id_srv_dst
                , cod_bank_dst
                , lib_etab_dst
                , famille_etab_dst
                , CASE
                    WHEN famille_etab_emt = 'TECHNIQUE'
                            OR famille_etab_dst = 'TECHNIQUE'
                        THEN 'TECHNIQUE'
                    WHEN Ifnull( famille_etab_emt, '_' ) = '_'
                            AND contrepartie = 'D'
                        THEN 'INCONNU'
                    WHEN Ifnull( famille_etab_dst, '_' ) = '_'
                            AND contrepartie = 'E'
                        THEN 'INCONNU'
                    WHEN Ifnull( famille_etab_emt, '_' ) = '_'
                            OR Ifnull( famille_etab_dst, '_' ) = '_'
                        THEN 'INTERBANCAIRE'
                    WHEN top_bq_identique = 'X'
                        THEN 'INTRABANCAIRE'
                    ELSE rfactq.qualifiant
                    END qualifiant_flux
                , contrepartie
                , tiers_op
                , type_remettant
                , cod_ent_ges_vb
                , cod_eve_ges_vb
                , cod_echange
                , cod_ind_cir
                , Concat(   cod_sens, 'µ', 'MT'
                            , msg_type, 'µ', canal_paiement
                            , 'µ', cd_devise, 'µ'
                            , cod_typ_op, 'µ', cod_typ_trt_frais
                            , 'µ', Ifnull( bic_emt, '_' ), 'µ'
                            , Ifnull( Substr( bic_emt, 5, 2 ), '_' ), 'µ', Ifnull( bic_dst, '_' )
                            , 'µ', Ifnull( Substr( bic_dst, 5, 2 ), '_' ), 'µ' ) crit_agregat
                , crit_supl_1
                , crit_supl_2
                , crit_supl_3
                , crit_supl_4
                , crit_supl_5
                , crit_supl_6
                , crit_supl_7
                , crit_supl_8
                , crit_supl_9
                , id_srv_ref
                , file_gen
                , creation_dt
                , year
                , month
                , quantite
                , mnt_dev
                , mnt_eur
        FROM tempcbk_fact_toge_t2 t2
            LEFT OUTER JOIN `tempcbk_fact_qualifiant` rfactq
                ON ( rfactq.groupe_1 = t2.famille_etab_emt
                        AND rfactq.groupe_2 = t2.famille_etab_dst ) )
    , tempcbk_bic_xenos AS (
        SELECT  code_xenos
                , code_banque
                , fib.cod_bic cod_bic
        FROM tempcbk_pays_xenos tpx
            LEFT JOIN "FLUX_REF"."REF_FIB_FGD" fib
                ON ( fib.cod_etab = tpx.code_banque ) )
    , tempcbk_typ_frais_e AS (
        SELECT  dat_ope_max
                , dat_partition
                , cod_transac
                , num_ce
                , num_ope
                , cvl_net_dbt
                , swift_banq_benef
                , com_chg_do
                , frais_corr_chg_do
                , CASE
                    WHEN com_chg_do = 'O'
                            AND frais_corr_chg_do = 'O'
                        THEN 'OUR'
                    WHEN com_chg_do = 'O'
                            AND frais_corr_chg_do = 'N'
                        THEN 'SHA'
                    WHEN com_chg_do = 'N'
                            AND frais_corr_chg_do = 'N'
                        THEN 'BEN'
                    ELSE '_'
                    END cod_typ_frais
        FROM "FLUX_INT"."NRM_XENOS_TRSF" trsf
        WHERE dat_partition BETWEEN (   SELECT date_debut_periode
                                        FROM date_parm )
                                 AND (  SELECT date_fin_periode
                                        FROM date_parm )
            AND cod_transac IN ( 'TRSF', 'OINT' )
        UNION ALL
        SELECT  dat_ope_max
                , dat_partition
                , cod_transac
                , num_ce
                , num_ope
                , cvl_net_dbt
                , swift_banq_benef
                , com_chg_do
                , frais_corr_chg_do
                , CASE
                    WHEN com_chg_do = 'O'
                            AND frais_corr_chg_do = 'O'
                        THEN 'OUR'
                    WHEN com_chg_do = 'O'
                            AND frais_corr_chg_do = 'N'
                        THEN 'SHA'
                    WHEN com_chg_do = 'N'
                            AND frais_corr_chg_do = 'N'
                        THEN 'BEN'
                    ELSE '_'
                    END cod_typ_frais
        FROM "FLUX_INT"."NRM_XENOS_TRSF_2024" trsf
        WHERE dat_partition BETWEEN (   SELECT date_debut_periode
                                        FROM date_parm )
                                 AND (  SELECT date_fin_periode
                                        FROM date_parm )
            AND cod_transac IN ( 'TRSF', 'OINT' ) )
    , tempcbk_xenos_e AS (
        SELECT   Cast( (    SELECT date_debut_periode
                            FROM date_parm ) AS DATE ) periode
                , date_vacation
                , caisse
                , transac
                , mode_traitement
                , devise
                , reference
                , dev_clt_do
                , bq_donneur_ordre
                , bq_beneficiaire
                , mnt_ordre
                , contre_val_ordre
                , 'E' cod_sens
                , CASE
                    WHEN bq_donneur_ordre IS NULL
                            OR Length( Trim( BQ_DONNEUR_ORDRE ) ) < 8
                        THEN '_'
                    WHEN Length( bq_donneur_ordre ) = 8
                        THEN Concat( bq_donneur_ordre, 'XXX' )
                    ELSE bq_donneur_ordre
                    END bic_emt
                , CASE
                    WHEN bq_beneficiaire IS NULL
                            OR Length( Trim( BQ_BENEFICIAIRE ) ) < 8
                        THEN '_'
                    WHEN Length( bq_beneficiaire ) = 8
                        THEN Concat( bq_beneficiaire, 'XXX' )
                    ELSE bq_beneficiaire
                    END bic_dst
                , CASE
                    WHEN Trim( caisse ) IS NULL
                            OR Length( Trim( caisse ) ) < 3
                        THEN '_'
                    ELSE caisse
                    END id_srv_emt
                , CASE
                    WHEN bq_beneficiaire IS NULL
                            OR Length( Trim( BQ_BENEFICIAIRE ) ) < 8
                        THEN '_'
                    WHEN Substr( bq_beneficiaire, 1, 4 ) IN ( 'CEPA', 'CCBP', 'NATX' )
                        THEN bq_beneficiaire
                    ELSE Substr( bq_beneficiaire, 1, 8 )
                    END id_srv_dst
                , CASE
                    WHEN transac IN ( 'TRSF' )
                        THEN '103'
                    WHEN transac IN ( 'OINT' )
                        THEN '202'
                    ELSE NULL
                    END msg_type
                , CASE
                    WHEN mode_traitement IS NULL
                            OR mode_traitement = 'AUTOM'
                        THEN 'ESTP'
                    ELSE 'EM'
                    END cod_typ_op
                , tfe.cod_typ_frais cod_typ_frais
        FROM "FLUX_INT"."NRM_XENOS_MENS" mens
            LEFT OUTER JOIN tempcbk_typ_frais_e tfe
                ON ( ( tfe.cod_transac = mens.transac
                            AND tfe.num_ope = mens.reference ) )
        WHERE mens.dat_partition BETWEEN (  SELECT date_debut_periode
                                            FROM date_parm )
                                     AND (  SELECT date_fin_periode
                                            FROM date_parm )
            AND mens.date_vacation BETWEEN (    SELECT date_debut_periode
                                                FROM date_parm )
                                         AND (  SELECT date_fin_periode
                                                FROM date_parm )
            AND mens.transac IN ( 'TRSF', 'OINT', 'ACCC' ) )
    , tempcbk_fact_xenos_t1_e AS (
        SELECT  periode
                , 'CBK' filiere
                , 'XENOS' application
                , CASE
                    WHEN tbx.cod_bic IS NULL
                        THEN 'BPCEFRPPXXX'
                    ELSE tbx.cod_bic
                    END cod_bic_ref
                , '_' cod_etab_ref
                , 'BPCEFRPPXXX' bic_chef_file
                , '16188' cd_bank_chef_file
                , '_' cd_bank_cl
                , CASE
                    WHEN femt.groupe IS NULL
                        THEN '_'
                    ELSE femt.groupe
                    END groupe
                , cod_sens
                , Ifnull( msg_type, '_' ) msg_type
                , '_' cod_canal_rgl
                , '_' lib_canal_rgl
                , '_' lib_sys_rgl
                , reference ref_op
                , '_' cod_unit
                , '_' lib_unit
                , '_' cod_evnt
                , '_' lib_evnt
                , 'XENOS' canal_paiement
                , '_' typ_ind
                , '_' id_statut
                , '_' lib_statut
                , CASE
                    WHEN femt.id_client IS NULL
                        THEN '_'
                    ELSE femt.id_client
                    END cod_client_fact
                , devise cd_devise
                , cod_typ_op
                , CASE
                    WHEN mode_traitement IS NULL
                            OR mode_traitement = 'AUTOM'
                        THEN 'Emis STP'
                    ELSE 'Emis Manuel'
                    END lib_typ_op
                , cod_typ_frais cod_typ_trt_frais
                , date_vacation date_echange
                , '_' lib_cod_signe_op
                , cod_typ_op cod_ope
                , '_' ss_cod_ope
                , '_' lib_ss_cod_ope
                , '_' cod_taxable
                , '_' lib_cod_taxable
                , '_' cd_moyen_pymnt
                , '_' lib_moyen_pymnt
                , '_' frais_trsp_dhl
                , '_' cd_pays_frais_trsp_dhl
                , bic_emt
                , CASE
                    WHEN bic_emt IS NULL
                            OR bic_emt = '_'
                        THEN '_'
                    ELSE Substr( bic_emt, 5, 2 )
                    END pays_emt
                , id_srv_emt
                , '_' cod_bank_emt
                , '_' lib_etab_emt
                , CASE
                    WHEN femt.groupe IS NULL
                        THEN 'INCONNU'
                    ELSE femt.groupe
                    END famille_etab_emt
                , bic_dst
                , CASE
                    WHEN bic_dst IS NULL
                            OR bic_dst = '_'
                        THEN '_'
                    ELSE Substr( bic_dst, 5, 2 )
                    END pays_dst
                , id_srv_dst
                , '_' cod_bank_dst
                , '_' lib_etab_dst
                , CASE
                    WHEN fdst.groupe IS NOT NULL
                        THEN fdst.groupe
                    ELSE fctrd.groupe
                    END famille_etab_dst
                , '_' qualifiant_flux
                , 'D' contrepartie
                , '_' tiers_op
                , '_' type_remettant
                , '_' cod_ent_ges_vb
                , '_' cod_eve_ges_vb
                , '_' cod_echange
                , '_' cod_ind_cir
                , '_' crit_agregat
                ,  Cast( NULL AS STRING ) crit_supl_1
                ,  Cast( NULL AS STRING ) crit_supl_2
                ,  Cast( NULL AS STRING ) crit_supl_3
                ,  Cast( NULL AS STRING ) crit_supl_4
                ,  Cast( NULL AS STRING ) crit_supl_5
                ,  Cast( NULL AS STRING ) crit_supl_6
                ,  Cast( NULL AS STRING ) crit_supl_7
                ,  Cast( NULL AS STRING ) crit_supl_8
                ,  Cast( NULL AS STRING ) crit_supl_9
                , id_srv_emt id_srv_ref
                , "" file_gen
                , CURRENT_TIMESTAMP creation_dt
                ,
                 Cast( Format_Date( '%G', periode ) AS INT ) YEAR
                ,
                 Cast( Format_Date( '%m', periode ) AS INT ) MONTH
                , 1 quantite
                , Round( mnt_ordre, 2 ) mnt_dev
                , Round( contre_val_ordre, 2 ) mnt_eur
        FROM tempcbk_xenos_e tpxe
            LEFT OUTER JOIN tempcbk_bic_xenos tbx
                ON ( tpxe.caisse = tbx.code_xenos )
            LEFT JOIN `tempcbk_fact_client_emt` femt
                ON ( femt.id_metier = tpxe.id_srv_emt
                        AND femt.application = 'XENOS' )
            LEFT JOIN `tempcbk_fact_client_dst` fdst
                ON ( fdst.id_metier = tpxe.id_srv_dst
                        AND fdst.application = 'XENOS' )
            LEFT JOIN `tempcbk_fact_client_ctrp` fctre
                ON ( fctre.id_metier = tpxe.id_srv_emt
                        AND fctre.application = 'CONTREPART' )
            LEFT JOIN `tempcbk_fact_client_ctrp` fctrd
                ON ( fctrd.id_metier = tpxe.id_srv_dst
                        AND fctrd.application = 'CONTREPART' )
        WHERE transac IN ( 'TRSF', 'OINT' ) )
    , tempcbk_fact_xenos_t1_echg AS (
        SELECT  periode
                , 'CBK' filiere
                , 'XENOS' application
                , CASE
                    WHEN tbx.cod_bic IS NULL
                        THEN 'BPCEFRPPXXX'
                    ELSE tbx.cod_bic
                    END cod_bic_ref
                , '_' cod_etab_ref
                , 'BPCEFRPPXXX' bic_chef_file
                , '16188' cd_bank_chef_file
                , '_' cd_bank_cl
                , CASE
                    WHEN femt.groupe IS NULL
                        THEN '_'
                    ELSE femt.groupe
                    END groupe
                , cod_sens
                , Ifnull( msg_type, '_' ) msg_type
                , '_' cod_canal_rgl
                , '_' lib_canal_rgl
                , '_' lib_sys_rgl
                , reference ref_op
                , '_' cod_unit
                , '_' lib_unit
                , '_' cod_evnt
                , '_' lib_evnt
                , 'XENOS' canal_paiement
                , '_' typ_ind
                , '_' id_statut
                , '_' lib_statut
                , CASE
                    WHEN femt.id_client IS NULL
                        THEN '_'
                    ELSE femt.id_client
                    END cod_client_fact
                , devise cd_devise
                , 'CHAH' cod_typ_op
                , 'Change . Achat de devises' lib_typ_op
                , '_' cod_typ_trt_frais
                , date_vacation date_echange
                , '_' lib_cod_signe_op
                , 'CHAH' cod_ope
                , '_' ss_cod_ope
                , '_' lib_ss_cod_ope
                , '_' cod_taxable
                , '_' lib_cod_taxable
                , '_' cd_moyen_pymnt
                , '_' lib_moyen_pymnt
                , '_' frais_trsp_dhl
                , '_' cd_pays_frais_trsp_dhl
                , bic_emt
                , CASE
                    WHEN bic_emt IS NULL
                            OR bic_emt = '_'
                        THEN '_'
                    ELSE Substr( bic_emt, 5, 2 )
                    END pays_emt
                , id_srv_emt
                , '_' cod_bank_emt
                , '_' lib_etab_emt
                , CASE
                    WHEN femt.groupe IS NULL
                        THEN 'INCONNU'
                    ELSE femt.groupe
                    END famille_etab_emt
                , bic_dst
                , CASE
                    WHEN bic_dst IS NULL
                            OR bic_dst = '_'
                        THEN '_'
                    ELSE Substr( bic_dst, 5, 2 )
                    END pays_dst
                , id_srv_dst
                , '_' cod_bank_dst
                , '_' lib_etab_dst
                , CASE
                    WHEN fdst.groupe IS NOT NULL
                        THEN fdst.groupe
                    ELSE fctrd.groupe
                    END famille_etab_dst
                , '_' qualifiant_flux
                , 'D' contrepartie
                , '_' tiers_op
                , '_' type_remettant
                , '_' cod_ent_ges_vb
                , '_' cod_eve_ges_vb
                , '_' cod_echange
                , '_' cod_ind_cir
                , '_' crit_agregat
                ,  Cast( NULL AS STRING ) crit_supl_1
                ,  Cast( NULL AS STRING ) crit_supl_2
                ,  Cast( NULL AS STRING ) crit_supl_3
                ,  Cast( NULL AS STRING ) crit_supl_4
                ,  Cast( NULL AS STRING ) crit_supl_5
                ,  Cast( NULL AS STRING ) crit_supl_6
                ,  Cast( NULL AS STRING ) crit_supl_7
                ,  Cast( NULL AS STRING ) crit_supl_8
                ,  Cast( NULL AS STRING ) crit_supl_9
                , id_srv_emt id_srv_ref
                , "" file_gen
                , CURRENT_TIMESTAMP creation_dt
                ,
                 Cast( Format_Date( '%G', periode ) AS INT ) YEAR
                ,
                 Cast( Format_Date( '%m', periode ) AS INT ) MONTH
                , 1 quantite
                , Round( mnt_ordre, 2 ) mnt_dev
                , Round( contre_val_ordre, 2 ) mnt_eur
        FROM tempcbk_xenos_e tpxe
            LEFT OUTER JOIN tempcbk_bic_xenos tbx
                ON ( tpxe.caisse = tbx.code_xenos )
            LEFT JOIN `tempcbk_fact_client_emt` femt
                ON ( femt.id_metier = tpxe.id_srv_emt
                        AND femt.application = 'XENOS' )
            LEFT JOIN `tempcbk_fact_client_dst` fdst
                ON ( fdst.id_metier = tpxe.id_srv_dst
                        AND fdst.application = 'XENOS' )
            LEFT JOIN `tempcbk_fact_client_ctrp` fctre
                ON ( fctre.id_metier = tpxe.id_srv_emt
                        AND fctre.application = 'CONTREPART' )
            LEFT JOIN `tempcbk_fact_client_ctrp` fctrd
                ON ( fctrd.id_metier = tpxe.id_srv_dst
                        AND fctrd.application = 'CONTREPART' )
        WHERE transac = 'ACCC'
            OR ( transac IN ( 'TRSF', 'OINT' )
                    AND dev_clt_do != devise
                    AND ( caisse IS NOT NULL
                            AND Length( Trim( caisse ) ) > 2 ) ) )
    , tempcbk_fact_xenos_all_e AS (
        SELECT *
        FROM tempcbk_fact_xenos_t1_e
        UNION ALL
        SELECT *
        FROM tempcbk_fact_xenos_t1_echg )
    , tempcbk_fact_xenos_e AS (
        SELECT  periode
                , filiere
                , application
                , cod_bic_ref
                , cod_etab_ref
                , bic_chef_file
                , cd_bank_chef_file
                , cd_bank_cl
                , groupe
                , cod_sens
                , msg_type
                , cod_canal_rgl
                , lib_canal_rgl
                , lib_sys_rgl
                , ref_op
                , cod_unit
                , lib_unit
                , cod_evnt
                , lib_evnt
                , canal_paiement
                , typ_ind
                , id_statut
                , lib_statut
                , cod_client_fact
                , cd_devise
                , cod_typ_op
                , lib_typ_op
                , cod_typ_trt_frais
                , date_echange
                , lib_cod_signe_op
                , cod_ope
                , ss_cod_ope
                , lib_ss_cod_ope
                , cod_taxable
                , lib_cod_taxable
                , cd_moyen_pymnt
                , lib_moyen_pymnt
                , frais_trsp_dhl
                , cd_pays_frais_trsp_dhl
                , bic_emt
                , pays_emt
                , id_srv_emt
                , Ifnull( cod_bank_emt, '_' ) cod_bank_emt
                , Ifnull( swfe.lib_inst_name, '_' ) lib_etab_emt
                , famille_etab_emt
                , bic_dst
                , pays_dst
                , id_srv_dst
                , Ifnull( cod_bank_dst, '_' ) cod_bank_dst
                , Ifnull( swfd.lib_inst_name, '_' ) lib_etab_dst
                , famille_etab_dst
                , CASE
                    WHEN famille_etab_emt = 'TECHNIQUE'
                            OR famille_etab_dst = 'TECHNIQUE'
                        THEN 'TECHNIQUE'
                    WHEN Ifnull( famille_etab_emt, '_' ) = '_'
                            AND contrepartie = 'D'
                        THEN 'INCONNU'
                    WHEN Ifnull( famille_etab_dst, '_' ) = '_'
                            AND contrepartie = 'E'
                        THEN 'INCONNU'
                    WHEN Ifnull( famille_etab_emt, '_' ) = '_'
                            OR Ifnull( famille_etab_dst, '_' ) = '_'
                        THEN 'INTERBANCAIRE'
                    ELSE rfactq.qualifiant
                    END qualifiant_flux
                , contrepartie
                , tiers_op
                , type_remettant
                , cod_ent_ges_vb
                , cod_eve_ges_vb
                , cod_echange
                , cod_ind_cir
                , Concat(   Ifnull( cod_sens, '_' ), 'µ', CASE
                                                            WHEN msg_type IS NULL
                                                                    OR msg_type = '_'
                                                                THEN '_'
                                                            ELSE Concat( 'MT', msg_type )
                                                            END
                            , 'µ', Ifnull( canal_paiement, '_' ), 'µ'
                            , Ifnull( cd_devise, '_' ), 'µ', Ifnull( cod_typ_op, '_' )
                            , 'µ', Ifnull( cod_typ_trt_frais, '_' ), 'µ'
                            , Ifnull( bic_emt, '_' ), 'µ', pays_emt
                            , 'µ', Ifnull( bic_dst, '_' ), 'µ'
                            , pays_dst, 'µ' ) crit_agregat
                , crit_supl_1
                , crit_supl_2
                , crit_supl_3
                , crit_supl_4
                , crit_supl_5
                , crit_supl_6
                , crit_supl_7
                , crit_supl_8
                , crit_supl_9
                , id_srv_ref
                , file_gen
                , creation_dt
                , year
                , month
                , quantite
                , mnt_dev
                , mnt_eur
        FROM tempcbk_fact_xenos_all_e t1e
            LEFT OUTER JOIN `tempcbk_fact_qualifiant` rfactq
                ON ( rfactq.groupe_1 = t1e.famille_etab_emt
                        AND rfactq.groupe_2 = t1e.famille_etab_dst )
            LEFT OUTER JOIN "FLUX_REF"."REF_BANQ_SWIFT" swfe
                ON ( t1e.bic_emt = swfe.cod_bic11 )
            LEFT OUTER JOIN "FLUX_REF"."REF_BANQ_SWIFT" swfd
                ON ( t1e.bic_dst = swfd.cod_bic11 ) )
    , tempcbk_typ_frais_r AS (
        SELECT DISTINCT
            dat_ope dat_ope_max
            , dat_partition
            , cod_transac
            , num_ope
            , cvl_net_dbt
            , bic_swift_bq_be swift_banq_benef
            , srecu_pl71a plg71a_cnce_srecu
        FROM "FLUX_INT"."NRM_XENOS_RAPT_2024" rapt
        WHERE dat_partition BETWEEN (   SELECT date_debut_periode
                                        FROM date_parm )
                                 AND (  SELECT date_fin_periode
                                        FROM date_parm )
            AND srecu_pl71a IS NOT NULL
            AND Length( srecu_pl71a ) > 2 )
    , tempcbk_xenos_r AS (
        SELECT   Cast( (    SELECT date_debut_periode
                            FROM date_parm ) AS DATE ) periode
                , date_vacation
                , caisse
                , transac
                , mode_traitement
                , devise
                , reference
                , dev_clt_do
                , bq_donneur_ordre
                , bq_beneficiaire
                , mnt_ordre
                , contre_val_ordre
                , 'R' cod_sens
                , CASE
                    WHEN bq_donneur_ordre IS NULL
                            OR Length( Trim( BQ_DONNEUR_ORDRE ) ) < 8
                        THEN '_'
                    WHEN Length( bq_donneur_ordre ) = 8
                        THEN Concat( bq_donneur_ordre, 'XXX' )
                    ELSE bq_donneur_ordre
                    END bic_emt
                , CASE
                    WHEN bq_beneficiaire IS NULL
                            OR Length( Trim( BQ_BENEFICIAIRE ) ) < 8
                        THEN '_'
                    WHEN Length( bq_beneficiaire ) = 8
                        THEN Concat( bq_beneficiaire, 'XXX' )
                    ELSE bq_beneficiaire
                    END bic_dst
                , CASE
                    WHEN bq_donneur_ordre IS NULL
                            OR Length( Trim( BQ_DONNEUR_ORDRE ) ) < 8
                        THEN '_'
                    WHEN Substr( bq_donneur_ordre, 1, 4 ) IN ( 'CEPA', 'CCBP', 'NATX' )
                        THEN bq_donneur_ordre
                    ELSE Substr( bq_donneur_ordre, 1, 8 )
                    END id_srv_emt
                , CASE
                    WHEN Trim( caisse ) IS NULL
                            OR Length( Trim( caisse ) ) < 3
                        THEN '_'
                    ELSE caisse
                    END id_srv_dst
                , CASE
                    WHEN transac IN ( 'RAPT', 'RAPR' )
                        THEN '103'
                    WHEN transac IN ( 'OINR' )
                        THEN '202'
                    ELSE NULL
                    END msg_type
                , CASE
                    WHEN mode_traitement IS NULL
                            OR mode_traitement = 'AUTOM'
                        THEN 'RSTP'
                    ELSE 'RM'
                    END cod_typ_op
                , Ifnull( tfr.plg71a_cnce_srecu, '_' ) code_typ_frais
        FROM "FLUX_INT"."NRM_XENOS_MENS" mens
            LEFT OUTER JOIN tempcbk_typ_frais_r tfr
                ON ( tfr.cod_transac = mens.transac
                        AND tfr.num_ope = mens.reference
                        AND tfr.cvl_net_dbt = mens.contre_val_ordre
                        AND tfr.swift_banq_benef = mens.bq_beneficiaire )
        WHERE mens.dat_partition BETWEEN (  SELECT date_debut_periode
                                            FROM date_parm )
                                     AND (  SELECT date_fin_periode
                                            FROM date_parm )
            AND mens.date_vacation BETWEEN (    SELECT date_debut_periode
                                                FROM date_parm )
                                         AND (  SELECT date_fin_periode
                                                FROM date_parm )
            AND mens.transac IN (   'OINR'
                                    , 'RAPT'
                                    , 'RAPR'
                                    , 'VTCC' ) )
    , tempcbk_fact_xenos_t1_r AS (
        SELECT  periode
                , 'CBK' filiere
                , 'XENOS' application
                , CASE
                    WHEN tbx.cod_bic IS NULL
                        THEN 'BPCEFRPPXXX'
                    ELSE tbx.cod_bic
                    END cod_bic_ref
                , '_' cod_etab_ref
                , 'BPCEFRPPXXX' bic_chef_file
                , '16188' cd_bank_chef_file
                , '_' cd_bank_cl
                , CASE
                    WHEN fdst.groupe IS NULL
                        THEN '_'
                    ELSE fdst.groupe
                    END groupe
                , cod_sens
                , Ifnull( msg_type, '_' ) msg_type
                , '_' cod_canal_rgl
                , '_' lib_canal_rgl
                , '_' lib_sys_rgl
                , reference ref_op
                , '_' cod_unit
                , '_' lib_unit
                , '_' cod_evnt
                , '_' lib_evnt
                , 'XENOS' canal_paiement
                , '_' typ_ind
                , '_' id_statut
                , '_' lib_statut
                , CASE
                    WHEN fdst.id_client IS NULL
                        THEN '_'
                    ELSE fdst.id_client
                    END cod_client_fact
                , devise cd_devise
                , cod_typ_op
                , CASE
                    WHEN mode_traitement IS NULL
                            OR mode_traitement = 'AUTOM'
                        THEN 'Recu STP'
                    ELSE 'Recu Manuel'
                    END lib_typ_op
                , code_typ_frais cod_typ_trt_frais
                , date_vacation date_echange
                , '_' lib_cod_signe_op
                , cod_typ_op cod_ope
                , '_' ss_cod_ope
                , '_' lib_ss_cod_ope
                , '_' cod_taxable
                , '_' lib_cod_taxable
                , '_' cd_moyen_pymnt
                , '_' lib_moyen_pymnt
                , '_' frais_trsp_dhl
                , '_' cd_pays_frais_trsp_dhl
                , bic_emt
                , CASE
                    WHEN bic_emt IS NULL
                            OR bic_emt = '_'
                        THEN '_'
                    ELSE Substr( bic_emt, 5, 2 )
                    END pays_emt
                , id_srv_emt
                , '_' cod_bank_emt
                , '_' lib_etab_emt
                , CASE
                    WHEN femt.groupe IS NOT NULL
                        THEN femt.groupe
                    ELSE fctre.groupe
                    END famille_etab_emt
                , bic_dst
                , CASE
                    WHEN bic_dst IS NULL
                            OR bic_dst = '_'
                        THEN '_'
                    ELSE Substr( bic_dst, 5, 2 )
                    END pays_dst
                , id_srv_dst
                , '_' cod_bank_dst
                , '_' lib_etab_dst
                , CASE
                    WHEN fdst.groupe IS NULL
                        THEN 'INCONNU'
                    ELSE fdst.groupe
                    END famille_etab_dst
                , '_' qualifiant_flux
                , 'E' contrepartie
                , '_' tiers_op
                , '_' type_remettant
                , '_' cod_ent_ges_vb
                , '_' cod_eve_ges_vb
                , '_' cod_echange
                , '_' cod_ind_cir
                , '_' crit_agregat
                ,  Cast( NULL AS STRING ) crit_supl_1
                ,  Cast( NULL AS STRING ) crit_supl_2
                ,  Cast( NULL AS STRING ) crit_supl_3
                ,  Cast( NULL AS STRING ) crit_supl_4
                ,  Cast( NULL AS STRING ) crit_supl_5
                ,  Cast( NULL AS STRING ) crit_supl_6
                ,  Cast( NULL AS STRING ) crit_supl_7
                ,  Cast( NULL AS STRING ) crit_supl_8
                ,  Cast( NULL AS STRING ) crit_supl_9
                , id_srv_dst id_srv_ref
                , "" file_gen
                , CURRENT_TIMESTAMP creation_dt
                ,
                 Cast( Format_Date( '%G', periode ) AS INT ) YEAR
                ,
                 Cast( Format_Date( '%m', periode ) AS INT ) MONTH
                , 1 quantite
                , Round( mnt_ordre, 2 ) mnt_dev
                , Round( contre_val_ordre, 2 ) mnt_eur
        FROM tempcbk_xenos_r tpxr
            LEFT OUTER JOIN tempcbk_bic_xenos tbx
                ON ( tpxr.caisse = tbx.code_xenos )
            LEFT JOIN `tempcbk_fact_client_emt` femt
                ON ( femt.id_metier = tpxr.id_srv_emt
                        AND femt.application = 'XENOS' )
            LEFT JOIN `tempcbk_fact_client_dst` fdst
                ON ( fdst.id_metier = tpxr.id_srv_dst
                        AND fdst.application = 'XENOS' )
            LEFT JOIN `tempcbk_fact_client_ctrp` fctre
                ON ( fctre.id_metier = tpxr.id_srv_emt
                        AND fctre.application = 'CONTREPART' )
            LEFT JOIN `tempcbk_fact_client_ctrp` fctrd
                ON ( fctrd.id_metier = tpxr.id_srv_dst
                        AND fctrd.application = 'CONTREPART' )
        WHERE transac IN ( 'RAPT', 'RAPR', 'OINR' ) )
    , tempcbk_fact_xenos_t1_rchg AS (
        SELECT  periode
                , 'CBK' filiere
                , 'XENOS' application
                , CASE
                    WHEN tbx.cod_bic IS NULL
                        THEN 'BPCEFRPPXXX'
                    ELSE tbx.cod_bic
                    END cod_bic_ref
                , '_' cod_etab_ref
                , 'BPCEFRPPXXX' bic_chef_file
                , '16188' cd_bank_chef_file
                , '_' cd_bank_cl
                , CASE
                    WHEN fdst.groupe IS NULL
                        THEN '_'
                    ELSE fdst.groupe
                    END groupe
                , cod_sens
                , Ifnull( msg_type, '_' ) msg_type
                , '_' cod_canal_rgl
                , '_' lib_canal_rgl
                , '_' lib_sys_rgl
                , reference ref_op
                , '_' cod_unit
                , '_' lib_unit
                , '_' cod_evnt
                , '_' lib_evnt
                , 'XENOS' canal_paiement
                , '_' typ_ind
                , '_' id_statut
                , '_' lib_statut
                , CASE
                    WHEN fdst.id_client IS NULL
                        THEN '_'
                    ELSE fdst.id_client
                    END cod_client_fact
                , devise cd_devise
                , 'CHVT'
                , 'Change . Vente de devises' lib_typ_op
                , '_' cod_typ_trt_frais
                , date_vacation date_echange
                , '_' lib_cod_signe_op
                , 'CHVT' cod_ope
                , '_' ss_cod_ope
                , '_' lib_ss_cod_ope
                , '_' cod_taxable
                , '_' lib_cod_taxable
                , '_' cd_moyen_pymnt
                , '_' lib_moyen_pymnt
                , '_' frais_trsp_dhl
                , '_' cd_pays_frais_trsp_dhl
                , bic_emt
                , CASE
                    WHEN bic_emt IS NULL
                            OR bic_emt = '_'
                        THEN '_'
                    ELSE Substr( bic_emt, 5, 2 )
                    END pays_emt
                , id_srv_emt
                , '_' cod_bank_emt
                , '_' lib_etab_emt
                , CASE
                    WHEN femt.groupe IS NOT NULL
                        THEN femt.groupe
                    ELSE fctre.groupe
                    END famille_etab_emt
                , bic_dst
                , CASE
                    WHEN bic_dst IS NULL
                            OR bic_dst = '_'
                        THEN '_'
                    ELSE Substr( bic_dst, 5, 2 )
                    END pays_dst
                , id_srv_dst
                , '_' cod_bank_dst
                , '_' lib_etab_dst
                , CASE
                    WHEN fdst.groupe IS NULL
                        THEN 'INCONNU'
                    ELSE fdst.groupe
                    END famille_etab_dst
                , '_' qualifiant_flux
                , 'E' contrepartie
                , '_' tiers_op
                , '_' type_remettant
                , '_' cod_ent_ges_vb
                , '_' cod_eve_ges_vb
                , '_' cod_echange
                , '_' cod_ind_cir
                , '_' crit_agregat
                ,  Cast( NULL AS STRING ) crit_supl_1
                ,  Cast( NULL AS STRING ) crit_supl_2
                ,  Cast( NULL AS STRING ) crit_supl_3
                ,  Cast( NULL AS STRING ) crit_supl_4
                ,  Cast( NULL AS STRING ) crit_supl_5
                ,  Cast( NULL AS STRING ) crit_supl_6
                ,  Cast( NULL AS STRING ) crit_supl_7
                ,  Cast( NULL AS STRING ) crit_supl_8
                ,  Cast( NULL AS STRING ) crit_supl_9
                , id_srv_dst id_srv_ref
                , "" file_gen
                , CURRENT_TIMESTAMP creation_dt
                , Cast( Format_Date( '%G', periode ) AS INT ) YEAR
                , Cast( Format_Date( '%m', periode ) AS INT ) MONTH
                , 1 quantite
                , Round( mnt_ordre, 2 ) mnt_dev
                , Round( contre_val_ordre, 2 ) mnt_eur
        FROM tempcbk_xenos_r tpxr
            LEFT OUTER JOIN tempcbk_bic_xenos tbx
                ON ( tpxr.caisse = tbx.code_xenos )
            LEFT JOIN `tempcbk_fact_client_emt` femt
                ON ( femt.id_metier = tpxr.id_srv_emt
                        AND femt.application = 'XENOS' )
            LEFT JOIN `tempcbk_fact_client_dst` fdst
                ON ( fdst.id_metier = tpxr.id_srv_dst
                        AND fdst.application = 'XENOS' )
            LEFT JOIN `tempcbk_fact_client_ctrp` fctre
                ON ( fctre.id_metier = tpxr.id_srv_emt
                        AND fctre.application = 'CONTREPART' )
            LEFT JOIN `tempcbk_fact_client_ctrp` fctrd
                ON ( fctrd.id_metier = tpxr.id_srv_dst
                        AND fctrd.application = 'CONTREPART' )
        WHERE transac = 'VTCC'
            OR ( transac IN ( 'RAPT', 'RAPR' )
                    AND dev_clt_do != devise
                    AND ( caisse IS NOT NULL
                            AND Length( Trim( caisse ) ) > 2 ) ) )
    , tempcbk_fact_xenos_all_r AS (
        SELECT *
        FROM tempcbk_fact_xenos_t1_r
        UNION ALL
        SELECT *
        FROM tempcbk_fact_xenos_t1_rchg )
    , tempcbk_fact_xenos_r AS (
        SELECT  periode
                , filiere
                , application
                , cod_bic_ref
                , cod_etab_ref
                , bic_chef_file
                , cd_bank_chef_file
                , cd_bank_cl
                , groupe
                , cod_sens
                , msg_type
                , cod_canal_rgl
                , lib_canal_rgl
                , lib_sys_rgl
                , ref_op
                , cod_unit
                , lib_unit
                , cod_evnt
                , lib_evnt
                , canal_paiement
                , typ_ind
                , id_statut
                , lib_statut
                , cod_client_fact
                , cd_devise
                , cod_typ_op
                , lib_typ_op
                , cod_typ_trt_frais
                , date_echange
                , lib_cod_signe_op
                , cod_ope
                , ss_cod_ope
                , lib_ss_cod_ope
                , cod_taxable
                , lib_cod_taxable
                , cd_moyen_pymnt
                , lib_moyen_pymnt
                , frais_trsp_dhl
                , cd_pays_frais_trsp_dhl
                , bic_emt
                , pays_emt
                , id_srv_emt
                , cod_bank_emt
                , Ifnull( swfe.lib_inst_name, '_' ) lib_etab_emt
                , famille_etab_emt
                , bic_dst
                , pays_dst
                , id_srv_dst
                , cod_bank_dst
                , Ifnull( swfd.lib_inst_name, '_' ) lib_etab_dst
                , famille_etab_dst
                , CASE
                    WHEN famille_etab_emt = 'TECHNIQUE'
                            OR famille_etab_dst = 'TECHNIQUE'
                        THEN 'TECHNIQUE'
                    WHEN Ifnull( famille_etab_emt, '_' ) = '_'
                            AND contrepartie = 'D'
                        THEN 'INCONNU'
                    WHEN Ifnull( famille_etab_dst, '_' ) = '_'
                            AND contrepartie = 'E'
                        THEN 'INCONNU'
                    WHEN Ifnull( famille_etab_emt, '_' ) = '_'
                            OR Ifnull( famille_etab_dst, '_' ) = '_'
                        THEN 'INTERBANCAIRE'
                    ELSE rfactq.qualifiant
                    END qualifiant_flux
                , contrepartie
                , tiers_op
                , type_remettant
                , cod_ent_ges_vb
                , cod_eve_ges_vb
                , cod_echange
                , cod_ind_cir
                , Concat(   Ifnull( cod_sens, '_' ), 'µ', CASE
                                                            WHEN msg_type IS NULL
                                                                    OR msg_type = '_'
                                                                THEN '_'
                                                            ELSE Concat( 'MT', msg_type )
                                                            END
                            , 'µ', Ifnull( canal_paiement, '_' ), 'µ'
                            , Ifnull( cd_devise, '_' ), 'µ', Ifnull( cod_typ_op, '_' )
                            , 'µ', Ifnull( cod_typ_trt_frais, '_' ), 'µ'
                            , Ifnull( bic_emt, '_' ), 'µ', pays_emt
                            , 'µ', Ifnull( bic_dst, '_' ), 'µ'
                            , pays_dst, 'µ' ) crit_agregat
                , crit_supl_1
                , crit_supl_2
                , crit_supl_3
                , crit_supl_4
                , crit_supl_5
                , crit_supl_6
                , crit_supl_7
                , crit_supl_8
                , crit_supl_9
                , id_srv_ref
                , file_gen
                , creation_dt
                , year
                , month
                , quantite
                , mnt_dev
                , mnt_eur
        FROM tempcbk_fact_xenos_all_r t1r
            LEFT OUTER JOIN `tempcbk_fact_qualifiant` rfactq
                ON ( rfactq.groupe_1 = t1r.famille_etab_emt
                        AND rfactq.groupe_2 = t1r.famille_etab_dst )
            LEFT OUTER JOIN "FLUX_REF"."REF_BANQ_SWIFT" swfe
                ON ( t1r.bic_emt = swfe.cod_bic11 )
            LEFT OUTER JOIN "FLUX_REF"."REF_BANQ_SWIFT" swfd
                ON ( t1r.bic_dst = swfd.cod_bic11 ) )
    , tempcbk_all_cbk AS (
        SELECT  periode
                , filiere
                , application
                , cod_bic_ref
                , cod_etab_ref
                , bic_chef_file
                , cd_bank_chef_file
                , cd_bank_cl
                , groupe
                , cod_sens
                , msg_type
                , cod_canal_rgl
                , lib_canal_rgl
                , lib_sys_rgl
                , ref_op
                , cod_unit
                , lib_unit
                , cod_evnt
                , lib_evnt
                , canal_paiement
                , typ_ind
                , id_statut
                , lib_statut
                , cod_client_fact
                , cd_devise
                , cod_typ_op
                , lib_typ_op
                , cod_typ_trt_frais
                , date_echange
                , lib_cod_signe_op
                , cod_ope
                , ss_cod_ope
                , lib_ss_cod_ope
                , cod_taxable
                , lib_cod_taxable
                , cd_moyen_pymnt
                , lib_moyen_pymnt
                , frais_trsp_dhl
                , cd_pays_frais_trsp_dhl
                , bic_emt
                , pays_emt
                , id_srv_emt
                , cod_bank_emt
                , lib_etab_emt
                , famille_etab_emt
                , bic_dst
                , pays_dst
                , id_srv_dst
                , cod_bank_dst
                , lib_etab_dst
                , famille_etab_dst
                , qualifiant_flux
                , contrepartie
                , tiers_op
                , type_remettant
                , cod_ent_ges_vb
                , cod_eve_ges_vb
                , cod_echange
                , cod_ind_cir
                , crit_agregat
                , crit_supl_1
                , crit_supl_2
                , crit_supl_3
                , crit_supl_4
                , crit_supl_5
                , crit_supl_6
                , crit_supl_7
                , crit_supl_8
                , crit_supl_9
                , id_srv_ref
                , file_gen
                , creation_dt
                , year
                , month
                , quantite
                , mnt_dev
                , mnt_eur
        FROM tempcbk_fact_cristal
        WHERE cod_client_fact = 'INCONNU'
        UNION ALL
        SELECT  periode
                , filiere
                , application
                , cod_bic_ref
                , cod_etab_ref
                , bic_chef_file
                , cd_bank_chef_file
                , cd_bank_cl
                , groupe
                , cod_sens
                , msg_type
                , cod_canal_rgl
                , lib_canal_rgl
                , lib_sys_rgl
                , ref_op
                , cod_unit
                , lib_unit
                , cod_evnt
                , lib_evnt
                , canal_paiement
                , typ_ind
                , id_statut
                , lib_statut
                , cod_client_fact
                , cd_devise
                , cod_typ_op
                , lib_typ_op
                , cod_typ_trt_frais
                , date_echange
                , lib_cod_signe_op
                , cod_ope
                , ss_cod_ope
                , lib_ss_cod_ope
                , cod_taxable
                , lib_cod_taxable
                , cd_moyen_pymnt
                , lib_moyen_pymnt
                , frais_trsp_dhl
                , cd_pays_frais_trsp_dhl
                , bic_emt
                , pays_emt
                , id_srv_emt
                , cod_bank_emt
                , lib_etab_emt
                , famille_etab_emt
                , bic_dst
                , pays_dst
                , id_srv_dst
                , cod_bank_dst
                , lib_etab_dst
                , famille_etab_dst
                , qualifiant_flux
                , contrepartie
                , tiers_op
                , type_remettant
                , cod_ent_ges_vb
                , cod_eve_ges_vb
                , cod_echange
                , cod_ind_cir
                , crit_agregat
                , crit_supl_1
                , crit_supl_2
                , crit_supl_3
                , crit_supl_4
                , crit_supl_5
                , crit_supl_6
                , crit_supl_7
                , crit_supl_8
                , crit_supl_9
                , id_srv_ref
                , file_gen
                , creation_dt
                , year
                , month
                , quantite
                , mnt_dev
                , mnt_eur
        FROM tempcbk_fact_tmt
        WHERE cod_client_fact = 'INCONNU'
        UNION ALL
        SELECT  periode
                , filiere
                , application
                , cod_bic_ref
                , cod_etab_ref
                , bic_chef_file
                , cd_bank_chef_file
                , cd_bank_cl
                , groupe
                , cod_sens
                , msg_type
                , cod_canal_rgl
                , lib_canal_rgl
                , lib_sys_rgl
                , ref_op
                , cod_unit
                , lib_unit
                , cod_evnt
                , lib_evnt
                , canal_paiement
                , typ_ind
                , id_statut
                , lib_statut
                , cod_client_fact
                , cd_devise
                , cod_typ_op
                , lib_typ_op
                , cod_typ_trt_frais
                , date_echange
                , lib_cod_signe_op
                , cod_ope
                , ss_cod_ope
                , lib_ss_cod_ope
                , cod_taxable
                , lib_cod_taxable
                , cd_moyen_pymnt
                , lib_moyen_pymnt
                , frais_trsp_dhl
                , cd_pays_frais_trsp_dhl
                , bic_emt
                , pays_emt
                , id_srv_emt
                , cod_bank_emt
                , lib_etab_emt
                , famille_etab_emt
                , bic_dst
                , pays_dst
                , id_srv_dst
                , cod_bank_dst
                , lib_etab_dst
                , famille_etab_dst
                , qualifiant_flux
                , contrepartie
                , tiers_op
                , type_remettant
                , cod_ent_ges_vb
                , cod_eve_ges_vb
                , cod_echange
                , cod_ind_cir
                , crit_agregat
                , crit_supl_1
                , crit_supl_2
                , crit_supl_3
                , crit_supl_4
                , crit_supl_5
                , crit_supl_6
                , crit_supl_7
                , crit_supl_8
                , crit_supl_9
                , id_srv_ref
                , file_gen
                , creation_dt
                , year
                , month
                , quantite
                , mnt_dev
                , mnt_eur
        FROM tempcbk_fact_toge
        WHERE cod_client_fact = 'INCONNU'
        UNION ALL
        SELECT  periode
                , filiere
                , application
                , cod_bic_ref
                , cod_etab_ref
                , bic_chef_file
                , cd_bank_chef_file
                , cd_bank_cl
                , groupe
                , cod_sens
                , msg_type
                , cod_canal_rgl
                , lib_canal_rgl
                , lib_sys_rgl
                , ref_op
                , cod_unit
                , lib_unit
                , cod_evnt
                , lib_evnt
                , canal_paiement
                , typ_ind
                , id_statut
                , lib_statut
                , cod_client_fact
                , cd_devise
                , cod_typ_op
                , lib_typ_op
                , cod_typ_trt_frais
                , date_echange
                , lib_cod_signe_op
                , cod_ope
                , ss_cod_ope
                , lib_ss_cod_ope
                , cod_taxable
                , lib_cod_taxable
                , cd_moyen_pymnt
                , lib_moyen_pymnt
                , frais_trsp_dhl
                , cd_pays_frais_trsp_dhl
                , bic_emt
                , pays_emt
                , id_srv_emt
                , cod_bank_emt
                , lib_etab_emt
                , famille_etab_emt
                , bic_dst
                , pays_dst
                , id_srv_dst
                , cod_bank_dst
                , lib_etab_dst
                , famille_etab_dst
                , qualifiant_flux
                , contrepartie
                , tiers_op
                , type_remettant
                , cod_ent_ges_vb
                , cod_eve_ges_vb
                , cod_echange
                , cod_ind_cir
                , crit_agregat
                , crit_supl_1
                , crit_supl_2
                , crit_supl_3
                , crit_supl_4
                , crit_supl_5
                , crit_supl_6
                , crit_supl_7
                , crit_supl_8
                , crit_supl_9
                , id_srv_ref
                , file_gen
                , creation_dt
                , year
                , month
                , quantite
                , mnt_dev
                , mnt_eur
        FROM tempcbk_fact_xenos_e
        WHERE cod_client_fact = 'INCONNU'
        UNION ALL
        SELECT  periode
                , filiere
                , application
                , cod_bic_ref
                , cod_etab_ref
                , bic_chef_file
                , cd_bank_chef_file
                , cd_bank_cl
                , groupe
                , cod_sens
                , msg_type
                , cod_canal_rgl
                , lib_canal_rgl
                , lib_sys_rgl
                , ref_op
                , cod_unit
                , lib_unit
                , cod_evnt
                , lib_evnt
                , canal_paiement
                , typ_ind
                , id_statut
                , lib_statut
                , cod_client_fact
                , cd_devise
                , cod_typ_op
                , lib_typ_op
                , cod_typ_trt_frais
                , date_echange
                , lib_cod_signe_op
                , cod_ope
                , ss_cod_ope
                , lib_ss_cod_ope
                , cod_taxable
                , lib_cod_taxable
                , cd_moyen_pymnt
                , lib_moyen_pymnt
                , frais_trsp_dhl
                , cd_pays_frais_trsp_dhl
                , bic_emt
                , pays_emt
                , id_srv_emt
                , cod_bank_emt
                , lib_etab_emt
                , famille_etab_emt
                , bic_dst
                , pays_dst
                , id_srv_dst
                , cod_bank_dst
                , lib_etab_dst
                , famille_etab_dst
                , qualifiant_flux
                , contrepartie
                , tiers_op
                , type_remettant
                , cod_ent_ges_vb
                , cod_eve_ges_vb
                , cod_echange
                , cod_ind_cir
                , crit_agregat
                , crit_supl_1
                , crit_supl_2
                , crit_supl_3
                , crit_supl_4
                , crit_supl_5
                , crit_supl_6
                , crit_supl_7
                , crit_supl_8
                , crit_supl_9
                , id_srv_ref
                , file_gen
                , creation_dt
                , year
                , month
                , quantite
                , mnt_dev
                , mnt_eur
        FROM tempcbk_fact_xenos_r
        WHERE cod_client_fact = 'INCONNU' )
    , step_id_cbk_all AS (
        SELECT DISTINCT
            'CBK' AS perimetre
            , application
            , id_srv_ref AS id_metier
        FROM tempcbk_all_cbk )
SELECT DISTINCT
    (   SELECT date_debut_periode
        FROM date_parm ) AS periode
    , perimetre
    , application
    , id_metier
    , ( SELECT date_trt
        FROM dat_trt ) AS trt_dat
FROM step_id_pfe_all
UNION ALL
SELECT DISTINCT
    (   SELECT date_debut_periode
        FROM date_parm ) AS periode
    , perimetre
    , application
    , id_metier
    , ( SELECT date_trt
        FROM dat_trt ) AS trt_dat
FROM step_id_esp_all
UNION ALL
SELECT DISTINCT
    (   SELECT date_debut_periode
        FROM date_parm ) AS periode
    , perimetre
    , application
    , id_metier
    , ( SELECT date_trt
        FROM dat_trt ) AS trt_dat
FROM step_id_bip_all
UNION ALL
SELECT DISTINCT
    (   SELECT date_debut_periode
        FROM date_parm ) AS periode
    , perimetre
    , application
    , id_metier
    , ( SELECT date_trt
        FROM dat_trt ) AS trt_dat
FROM step_id_swift_all
UNION ALL
SELECT DISTINCT
    (   SELECT date_debut_periode
        FROM date_parm ) AS periode
    , perimetre
    , application
    , id_metier
    , ( SELECT date_trt
        FROM dat_trt ) AS trt_dat
FROM step_id_gm_all
UNION ALL
SELECT DISTINCT
    (   SELECT date_debut_periode
        FROM date_parm ) AS periode
    , perimetre
    , application
    , id_metier
    , ( SELECT date_trt
        FROM dat_trt ) AS trt_dat
FROM step_id_cbk_all
;